package com.sunildhiman90.kmauth.google

import co.touchlab.kermit.Logger
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.gson.Gson
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthUser
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID


internal class GoogleAuthManagerJvm : GoogleAuthManager {

    private var webClientId: String
    private var clientSecret: String
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? =
        null

    private val redirectUri = "http://localhost:8080/callback" // Ktor will listen on this URI
    private var uniqueUserId: String? = null
    private var onSignResult: ((KMAuthUser?, Throwable?) -> Unit)? = null

    init {

        require(!KMAuthInitializer.getWebClientId().isNullOrEmpty()) {
            val message =
                "webClientId should not be null or empty, Please set it in KMAuthInitializer::init"
            Logger.withTag(TAG).e(message)
            message
        }

        webClientId = KMAuthInitializer.getWebClientId()!!

        require(!KMAuthInitializer.getClientSecret().isNullOrEmpty()) {
            val message =
                "clientSecret should not be null or empty, Please set it in KMAuthInitializer::init or KMAuthInitializer::initClientSecret"
            Logger.withTag(TAG).e(message)
            message
        }

        webClientId = KMAuthInitializer.getWebClientId()!!
        clientSecret = KMAuthInitializer.getClientSecret()!!

    }

    override suspend fun signIn(onSignResult: (KMAuthUser?, Throwable?) -> Unit) {
        this.onSignResult = onSignResult
        launchGoogleSignIn()
    }

    // Start the Ktor HTTP server to handle the OAuth redirect response
    private fun startHttpServer(flow: GoogleAuthorizationCodeFlow, port: Int = 8080): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
        Logger.d("Starting HTTP server on port $port")
        val server = embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json()
            }

            routing {
                get("/callback") {
                    Logger.d("Received callback: ${call.request.queryParameters}")
                    // Capture the authorization code from the URL
                    val code = call.request.queryParameters["code"] ?: ""
                    Logger.d("Received callback code: $code")

                    if (code.isNotEmpty()) {
                        try {

                            // Exchange the code for an access token
                            val tokenResponse: GoogleTokenResponse = flow.newTokenRequest(code)
                                .setRedirectUri(redirectUri)
                                .execute()

                            uniqueUserId = UUID.randomUUID().toString()
                            val credential =
                                flow.createAndStoreCredential(tokenResponse, uniqueUserId!!)

                            // Fetch user info
                            val requestFactory: HttpRequestFactory =
                                NetHttpTransport().createRequestFactory(credential)

                            val url = GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo")
                            val request = requestFactory.buildGetRequest(url)
                            val response = request.execute()
                            val userInfoString = response.parseAsString()
                            val userInfo = Gson().fromJson(userInfoString, GoogleUser::class.java)

                            onSignResult?.invoke(
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

                    Logger.d("Shutting down server")
                    server?.stop(1000, 1000)

                }
            }
        }.start(wait = false)
        return server
    }

    private fun launchGoogleSignIn() {

        try {
            // We are using google-api-client, alternatively we can use core oauth2 url as well i.e.
            // https://accounts.google.com/o/oauth2/v2/auth?
            // scope=email%20profile&
            // response_type=code&
            // state=security_token%3D138r5719ru3e1%26url%3Dhttps%3A%2F%2Foauth2.example.com%2Ftoken&
            // redirect_uri=com.example.app%3A/oauth2redirect&
            // client_id=client_id

            val flow = initializeGoogleAuthCodeFlow()

            val authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri).build()

            // Open the user's default web browser to authenticate
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(authorizationUrl))
            }

            // Start the HTTP server in a separate thread, otherwise it will block the ui
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                server = startHttpServer(flow)
            }.invokeOnCompletion {
                Logger.d("invokeOnCompletion called")
                scope.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onSignResult?.invoke(null, e)
            Logger.e(e.message.toString())
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

    private data class GoogleUser(
        val id: String,
        val email: String,
        val verified_email: Boolean,
        val name: String,
        val given_name: String,
        val family_name: String,
        val picture: String
    )

    companion object {
        private const val TAG = "GoogleAuthManagerJvm"
    }
}