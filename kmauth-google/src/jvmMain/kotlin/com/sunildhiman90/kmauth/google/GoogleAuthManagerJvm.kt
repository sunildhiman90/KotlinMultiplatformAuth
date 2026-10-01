package com.sunildhiman90.kmauth.google

import co.touchlab.kermit.Logger
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthUser
import io.ktor.client.HttpClient as KtorHttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import kotlin.coroutines.resume

@Serializable
internal data class GoogleOAuthTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("id_token")
    val idToken: String? = null,
    @SerialName("expires_in")
    val expiresIn: Long? = null,
    @SerialName("token_type")
    val tokenType: String? = null,
    @SerialName("scope")
    val scope: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null
)

internal class GoogleAuthManagerJvm : GoogleAuthManager {

    private var webClientId: String
    private var clientSecret: String
    private var codeVerifier: String? = null
    private var ktorClient: KtorHttpClient? = null
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? =
        null

    private var actualPort: Int = DEFAULT_PORT
    private val redirectUri: String
        get() {
            val host = KMAuthInitializer.getGoogleClientRedirectHost(providerId) ?: "localhost"
            val hostWithoutPort = host.split(":").first()
            val scheme = KMAuthInitializer.getGoogleClientRedirectScheme(providerId) ?: "http"
            return "$scheme://$hostWithoutPort:$actualPort/callback"
        }

    private fun getPort(): Int {
        val host = KMAuthInitializer.getGoogleClientRedirectHost(providerId) ?: return DEFAULT_PORT
        return host.split(":").lastOrNull()?.toIntOrNull() ?: DEFAULT_PORT
    }

    private var uniqueUserId: String? = null
    private var onSignResult: ((KMAuthUser?, Throwable?) -> Unit)? = null
    private var scope = CoroutineScope(Dispatchers.IO)

    init {
        require(!KMAuthInitializer.getWebClientId(providerId).isNullOrEmpty()) {
            val message =
                "webClientId should not be null or empty, Please set it in KMAuthInitializer::initialize(KMAuthConfig.forGoogle)"
            Logger.withTag(TAG).e(message)
            message
        }

        require(!KMAuthInitializer.getClientSecret(providerId).isNullOrEmpty()) {
            val message =
                "(GoogleAuthManagerJvm) clientSecret should not be null or empty, Please set it in KMAuthInitializer::initialize(KMAuthConfig.forGoogle)"
            Logger.withTag(TAG).e(message)
            message
        }

        webClientId = KMAuthInitializer.getWebClientId(providerId)!!
        clientSecret = KMAuthInitializer.getClientSecret(providerId)!!
    }

    override suspend fun signIn(onSignResult: (KMAuthUser?, Throwable?) -> Unit) {
        this.onSignResult = onSignResult
        launchGoogleSignIn()
    }

    override suspend fun signIn(): Result<KMAuthUser?> {
        return suspendCancellableCoroutine { continuation ->
            val onSignResult: (KMAuthUser?, Throwable?) -> Unit = { user, error ->
                if (error == null) {
                    // Resume coroutine with an exception provided by the callback
                    continuation.resume(Result.success(user))
                } else {
                    // Resume coroutine with a value provided by the callback
                    continuation.resume(Result.failure(error))
                }
            }

            launchGoogleSignIn(onSignResult)

            continuation.invokeOnCancellation {
                codeVerifier = null
                performShutdownCleanup()
            }
        }
    }

    // Start the Ktor HTTP server to handle the OAuth redirect response
    @OptIn(DelicateCoroutinesApi::class)
    private fun startHttpServer(
        flow: GoogleAuthorizationCodeFlow,
        onSignResult: ((KMAuthUser?, Throwable?) -> Unit)? = null,
        port: Int = actualPort
    ): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
        Logger.d("Starting HTTP server on port $port")
        val server = embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json()
            }

            routing {
                get("/callback") {
                    Logger.withTag(TAG).d("Received OAuth redirect callback.")
                    // Capture the authorization code from the URL
                    val code = call.request.queryParameters["code"] ?: ""

                    if (code.isNotEmpty()) {
                        Logger.withTag(TAG).d("Authorization code received, exchanging for tokens.")
                        try {
                            // Exchange the code for an access token via Ktor POST request with PKCE
                            val tokenResponse = exchangeCodeForToken(code, codeVerifier)

                            uniqueUserId = UUID.randomUUID().toString()
                            val googleTokenResponse = GoogleTokenResponse().apply {
                                accessToken = tokenResponse.accessToken
                                idToken = tokenResponse.idToken
                                expiresInSeconds = tokenResponse.expiresIn
                                tokenType = tokenResponse.tokenType
                                scope = tokenResponse.scope
                                refreshToken = tokenResponse.refreshToken
                            }
                            flow.createAndStoreCredential(googleTokenResponse, uniqueUserId!!)

                            // Fetch user info
                            val userInfo = fetchGoogleUserInfo(tokenResponse.accessToken)

                            val callback = onSignResult ?: this@GoogleAuthManagerJvm.onSignResult
                            callback?.invoke(
                                KMAuthUser(
                                    id = userInfo.id,
                                    idToken = tokenResponse.idToken,
                                    accessToken = tokenResponse.accessToken,
                                    name = userInfo.name,
                                    email = userInfo.email,
                                    profilePicUrl = userInfo.picture
                                ),
                                null
                            )
                            Logger.d("Authentication successful.")
                            // Send response back to the client
                            call.respondText(
                                "Authentication successful. You can close this window and return to the app",
                                ContentType.Text.Plain,
                                HttpStatusCode.OK
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Logger.e(e.message.toString())
                            call.respondText(
                                "Some error in receiving code parameter: $e, Please try again from app",
                                ContentType.Text.Plain,
                                HttpStatusCode.BadRequest
                            )
                        }
                    } else {
                        Logger.d("Missing code parameter.")
                        call.respondText(
                            "Missing code parameter.Please try again from app",
                            ContentType.Text.Plain,
                            HttpStatusCode.BadRequest
                        )
                    }

                    // 🔁 Then shutdown outside of the Ktor pipeline
                    performShutdownCleanup()

                }
            }
        }.start(wait = false)
        return server
    }

    private fun getKtorClient(): KtorHttpClient {
        return ktorClient ?: KtorHttpClient(CIO) {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(json)
            }
        }.also { ktorClient = it }
    }

    private suspend fun exchangeCodeForToken(
        code: String,
        codeVerifier: String?
    ): GoogleOAuthTokenResponse {
        val client = getKtorClient()
        val response = client.submitForm(
            url = GOOGLE_TOKEN_URL,
            formParameters = parameters {
                append("client_id", webClientId)
                append("code", code)
                append("redirect_uri", redirectUri)
                append("grant_type", "authorization_code")
                if (!codeVerifier.isNullOrEmpty()) {
                    append("code_verifier", codeVerifier)
                }
                append("client_secret", clientSecret)
            }
        )

        val responseBody = response.bodyAsText()
        if (!response.status.isSuccess()) {
            Logger.withTag(TAG).e("Token exchange failed with status ${response.status}: $responseBody")
            throw IllegalStateException("Token exchange failed with status ${response.status}: $responseBody")
        }

        return json.decodeFromString<GoogleOAuthTokenResponse>(responseBody)
    }

    private suspend fun fetchGoogleUserInfo(accessToken: String): GoogleUser {
        val client = getKtorClient()
        val response = client.get(GOOGLE_USER_INFO_URL) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
                append(HttpHeaders.Accept, "application/json")
            }
        }
        val responseBody = response.bodyAsText()
        if (!response.status.isSuccess()) {
            Logger.withTag(TAG).e("Fetching user info failed with status ${response.status}: $responseBody")
            throw IllegalStateException("Fetching user info failed with status ${response.status}: $responseBody")
        }
        return json.decodeFromString<GoogleUser>(responseBody)
    }

    private fun findAvailablePort(startPort: Int): Int {
        var port = startPort
        while (port < startPort + 100) {
            try {
                java.net.ServerSocket(port).use { return port }
            } catch (e: Exception) {
                port++
            }
        }
        return startPort
    }

    private fun performShutdownCleanup() {
        scope.launch {
            try {
                delay(500) // wait for response to be sent
                Logger.d("Shutting down server")
                server?.stop(1000, 1000) // Stop the server gracefully
                codeVerifier = null
                try {
                    ktorClient?.close()
                } catch (_: Exception) {}
                ktorClient = null
                scope.cancel()
            } catch (e: Exception) {
                Logger.e("Error shutting down server: ${e.message}")
                codeVerifier = null
                try {
                    ktorClient?.close()
                } catch (_: Exception) {}
                ktorClient = null
                scope.cancel()  // Ensure scope is cancelled even if there's an error
            }
        }
    }

    private fun launchGoogleSignIn(
        onSignResult: ((KMAuthUser?, Throwable?) -> Unit)? = null
    ) {

        try {
            clientSecret = KMAuthInitializer.getClientSecret(providerId) ?: clientSecret

            // Generate PKCE code verifier and code challenge
            val verifier = PkceUtils.generateCodeVerifier()
            codeVerifier = verifier
            val codeChallenge = PkceUtils.generateCodeChallenge(verifier)

            val flow = initializeGoogleAuthCodeFlow()

            actualPort = getPort()
            Logger.d("Using Redirect URI: $redirectUri")
            val authorizationUrl = flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .set("code_challenge", codeChallenge)
                .set("code_challenge_method", "S256")
                .build()
            Logger.d("Opening Authorization URL: $authorizationUrl")

            // Open the user's default web browser to authenticate
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(authorizationUrl))
            }

            // Start the HTTP server in a separate thread, otherwise it will block the ui
            // We need to reinitialize the scope, otherwise it will throw exception second time becoz we are cancelling the scope after stopping the server and cancelled scope can not be used to launch coroutine again without recreating new scope
            scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    server = startHttpServer(flow, onSignResult, actualPort)
                } catch (e: Exception) {
                    val errorMessage = if (e is java.net.BindException || e.message?.contains("Address already in use") == true) {
                        "Port $actualPort is already in use. Please ensure no other service is using this port or configure a different port in KMAuthConfig.forGoogle(googleClientRedirectHost = \"localhost:YOUR_PORT\") and register it in Google Cloud Console web client as well."
                    } else {
                        "Failed to start local server for Google Auth: ${e.message}"
                    }
                    Logger.e(errorMessage)
                    onSignResult?.invoke(null, Exception(errorMessage, e))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Not able to start the server: ${e.message.toString()}")
            onSignResult?.invoke(null, e)
        }
    }

    private fun initializeGoogleAuthCodeFlow(): GoogleAuthorizationCodeFlow {
        val flow = GoogleAuthorizationCodeFlow.Builder(
            NetHttpTransport(), GsonFactory.getDefaultInstance(), webClientId, clientSecret,
            listOf(
                "https://www.googleapis.com/auth/userinfo.profile",
                "https://www.googleapis.com/auth/userinfo.email"
            )
        ).setDataStoreFactory(FileDataStoreFactory(File("tokens")))
            .build()
        return flow
    }

    private suspend fun revokeToken(flow: GoogleAuthorizationCodeFlow, userId: String): Boolean {
        val credential: Credential? = flow.loadCredential(userId)
        val accessToken = credential?.accessToken

        if (accessToken == null) {
            Logger.d("No valid access token found for user: $userId")
            return false
        }

        val client = HttpClient.newHttpClient()
        return try {
            withContext(Dispatchers.IO) {
                val httpRequest = HttpRequest.newBuilder()
                    .uri(URI("https://oauth2.googleapis.com/revoke"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("token=$accessToken"))
                    .build()
                val response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() != HttpStatusCode.OK.value) {
                    Logger.d("Failed to revoke token: ${response.body()}")
                    false
                } else {
                    Logger.d("Token revoked successfully.")
                    true
                }
            }
        } catch (e: Exception) {
            Logger.d("Failed to revoke token: ${e.message}")
            false
        }
    }

    // Clear stored credentials
    private fun clearStoredCredentials(flow: GoogleAuthorizationCodeFlow, userId: String) {
        flow.credentialDataStore?.delete(userId)
        Logger.d("Stored credentials cleared for user: $userId")
    }

    // Sign out function
    override suspend fun signOut(userId: String?) {
        //Alternatively, we can save user id in shared preferences after login and reuse that
        if (userId != null) {
            val flow = initializeGoogleAuthCodeFlow() // Reinitialize the flow
            val revoked = revokeToken(flow, userId)
            if (revoked) {
                clearStoredCredentials(flow, userId)
                Logger.withTag("signOut").d("User successfully signed out.")
            } else {
                Logger.withTag("signOut").d("Sign-out failed.")
            }
        } else {
            Logger.withTag("signOut").d("User id is null.")
        }
    }

    companion object {
        private const val TAG = "GoogleAuthManagerJvm"
        private const val DEFAULT_PORT = 8080
        private const val GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token"
        private const val GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
