package com.sunildhiman90.kmauth.google

import co.touchlab.kermit.Logger
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthUser
import com.sunildhiman90.kmauth.google.externals.CredentialResponse
import com.sunildhiman90.kmauth.google.externals.GoogleUserJs
import com.sunildhiman90.kmauth.google.externals.TokenClientConfig
import com.sunildhiman90.kmauth.google.externals.TokenResponse
import com.sunildhiman90.kmauth.google.externals.google
import com.sunildhiman90.kmauth.google.jsUtils.convertToGoogleUserInfo
import com.sunildhiman90.kmauth.google.jsUtils.googleIdConfig
import com.sunildhiman90.kmauth.google.jsUtils.overrideTokenClientConfig
import com.sunildhiman90.kmauth.google.jsUtils.tokenClientConfig
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.w3c.dom.HTMLScriptElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import org.w3c.fetch.CORS
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode
import org.w3c.fetch.Response
import kotlin.coroutines.resume

const val GSI_CLIENT_URL = "https://accounts.google.com/gsi/client"
const val GOOGLE_BUTTON_ID = "gid"

internal class GoogleAuthManagerJs : GoogleAuthManager {

    private var isLibraryLoaded: Boolean = false
    private var isGoogleClientInitialized: Boolean = false
    private var webClientId: String

    private var onSignResult: ((KMAuthUser?, Throwable?) -> Unit)? = null

    init {

        require(!KMAuthInitializer.getWebClientId().isNullOrEmpty()) {
            val message =
                "webClientId should not be null or empty, Please set it in KMAuthInitializer::init"
            Logger.withTag(TAG).e(message)
            message
        }

        webClientId = KMAuthInitializer.getWebClientId()!!

        loadGoogleSignInLibrary {
            console.log("Google sign in library loaded")
            isLibraryLoaded = true

            //for one tap prompt
            initializeGoogleSignIn(
                webClientId,
                onError = {
                    Logger.i("initializeGoogleSignIn: onError")
                    onSignResult?.invoke(null, Exception("Google sign in failed"))
                },
                onSuccess = { credential ->
                    Logger.i("initializeGoogleSignIn: onSuccess")
                    // Decode the JWT token to get the user's information
                    val userInfo = decodeJwtPayload(credential)?.jsonObject

                    if (userInfo != null) {
                        val sub = userInfo["sub"]?.jsonPrimitive
                        val name = userInfo["name"]?.jsonPrimitive
                        val email = userInfo["email"]?.jsonPrimitive
                        val picture = userInfo["picture"]?.jsonPrimitive

                        onSignResult?.invoke(
                            KMAuthUser(
                                id = sub?.content ?: "",
                                idToken = credential,
                                name = name?.content ?: "",
                                email = email?.content ?: "",
                                profilePicUrl = picture?.content ?: ""
                            ), null
                        )
                    } else {
                        onSignResult?.invoke(null, Exception("Google sign in failed"))
                    }
                },
            )
        }
    }


    private fun loadGoogleSignInLibrary(onLoad: () -> Unit) {
        val script =
            document.createElement("script") as HTMLScriptElement // Create the script element
        script.src = GSI_CLIENT_URL
        script.async = true
        script.defer = true

        //If it does not work, we should use this: https://developers.google.com/identity/gsi/web/reference/js-reference#onGoogleLibraryLoad
        script.onload = { onLoad() }

        var alreadyAdded = false
        val existingScripts = document.head?.getElementsByTagName("script")
        val nodesLength = existingScripts?.length ?: 0
        for (i in 0 until nodesLength) {
            if ((existingScripts?.get(i) as HTMLScriptElement).src == GSI_CLIENT_URL) {
                alreadyAdded = true
            }
        }
        // Append the script to the document head
        if (!alreadyAdded) document.head?.appendChild(script)
    }

    private fun initializeGoogleSignIn(
        clientId: String,
        onSuccess: (String) -> Unit,
        onError: (() -> Unit)? = null
    ) {

        console.info("initializeGoogleSignIn")

        require(clientId.isNotEmpty()) {
            val error = "clientId should not be null or empty"
            console.log(error)
            error
        }

        check(isLibraryLoaded) {
            val error = "Google sign in library is not loaded"
            console.log(error)
            error
        }

        if (isLibraryLoaded) {

            try {

                initializeGoogleAccountId(onSuccess, onError, clientId)

            } catch (e: dynamic) {
                console.error("Google Sign-In initialization failed: $e")
                isGoogleClientInitialized = false
                onError?.invoke()
            }

        }

    }

    private fun initializeGoogleAccountId(
        onSuccess: (String) -> Unit,
        onError: (() -> Unit)? = null,
        clientId: String
    ) {
        val callbackFunction: (CredentialResponse) -> Unit = { response ->
            Logger.d("initializeGoogleAccountId: callbackFunction")
            isGoogleClientInitialized = true

            // Get the credential (JWT token) from the response,
            // credential:  this field is the ID token as a base64-encoded JSON Web Token (JWT) string
            val credential = response.credential
            if (!credential.isNullOrEmpty()) {
                onSuccess(credential)
            } else {
                onError?.invoke()
            }
        }

        val config = googleIdConfig(clientId, callbackFunction)
        google.accounts.id.initialize(config)
    }

    private fun oneTapPromptGoogleSignIn() {
        console.log("promptGoogleSignIn:")
        google.accounts.id.prompt { notification: google.accounts.id.PromptMomentNotification ->
            if (notification.isSkippedMoment() == true) {
                console.info("promptGoogleSignIn one tap isNotDisplayed_or_isSkipped:")
                //trigger rendered button click
                document.getElementById(GOOGLE_BUTTON_ID)
                    ?.querySelector("div[role='button']")
                    ?.dispatchEvent(
                        Event("click")
                    )
            }
            if (notification.isDismissedMoment() == true) {
                //if "credential_returned", it means user has already signed in successfully
                console.info("promptGoogleSignIn callback dismissed_reason: ${notification.getDismissedReason()}")
            }
        }
    }

    private fun decodeJwtPayload(jwt: String): JsonElement? {
        // JWTs are Base64URL encoded. Split the token and decode the payload part.
        return try {
            val payload = jwt.split(".")[1] // The payload is the second part of the JWT
            val decodedPayload = window.atob(payload) // Decode Base64URL to a string
            Json.parseToJsonElement(decodedPayload)
        } catch (e: Exception) {
            console.error("Failed to decode JWT payload", e)
            null
        }
    }

    override suspend fun signIn(
        onSignResult: (KMAuthUser?, Throwable?) -> Unit
    ) {
        signInCore(onSignResult = onSignResult, scopes = Scopes.defaultScopes)
    }

    override suspend fun signIn(): Result<KMAuthUser?> {
        return suspendCancellableCoroutine { continuation ->
            signInCore(continuation = continuation, scopes = Scopes.defaultScopes)
        }
    }

    private fun signInCore(
        continuation: CancellableContinuation<Result<KMAuthUser?>>? = null,
        onSignResult: ((KMAuthUser?, Throwable?) -> Unit)? = null,
        scopes: List<String>
    ) {
        val callbackFunction: (TokenResponse) -> Unit = { response ->
            Logger.d("initTokenClient: callbackFunction")

            val accessToken = response.access_token
            val error = response.error
            if (error != null) {
                continuation?.resume(Result.failure(Exception("Failed to get access token: $error")))
                onSignResult?.invoke(null, Exception("Failed to get access token: $error"))
            } else {

                if (!accessToken.isNullOrEmpty()) {

                    val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
                        continuation?.resume(Result.failure(exception))
                        onSignResult?.invoke(null, exception)
                    }

                    val context = continuation?.context ?: Dispatchers.Default
                    CoroutineScope(context + coroutineExceptionHandler).launch {
                        val userInfo = getGoogleUserJsInfo(accessToken)
                        if (userInfo?.id != null) {
                            val user = KMAuthUser(
                                id = userInfo.id!!,
                                name = userInfo.name,
                                email = userInfo.email,
                                accessToken = accessToken,
                                profilePicUrl = userInfo.picture
                            )
                            continuation?.resume(Result.success(user))
                            onSignResult?.invoke(user, null)
                        } else {
                            continuation?.resume(Result.failure(Exception("Failed to get user info")))
                            onSignResult?.invoke(null, Exception("Failed to get user info"))
                        }
                    }
                } else {
                    continuation?.resume(Result.failure(Exception("Access token is empty")))
                    onSignResult?.invoke(null, Exception("Access token is empty"))
                }
            }
        }

        val config: TokenClientConfig = tokenClientConfig(
            clientId = webClientId,
            scope = scopes.joinToString(" "),
            callback = callbackFunction
        )

        val client = google.accounts.oauth2.initTokenClient(
            config
        )

        val overrideConfig = overrideTokenClientConfig(
            scope = scopes.joinToString(" "),
            includeGrantedScopes = true,
            prompt = GoogleTokenClientConfigPrompt.SELECT_ACCOUNT.value,
        )

        //Use the requestAccessToken() method to trigger the token UX flow and obtain an access token. Google prompts the user to:
        //Choose their account,
        //sign-in to the Google Account if not already signed-in,
        //grant consent for your web app to access each requested scope.
        client.requestAccessToken(overrideConfig)
    }

    override suspend fun signOut(userId: String?) {
        google.accounts.id.disableAutoSelect()
        this.onSignResult = null
    }

    private suspend fun getGoogleUserJsInfo(accessToken: String): GoogleUserJs? {
        try {

            val url = "https://www.googleapis.com/oauth2/v2/userinfo"
            val headers = Headers()
            headers.append("Content-Type", "application/json")
            headers.append("Authorization", "Bearer $accessToken")

            val response: Response = window.fetch(
                url,
                RequestInit(
                    method = "GET",
                    headers = headers,
                    mode = RequestMode.CORS
                )
            ).await()

            if (!response.ok) {
                val error = response.statusText
                Logger.e("Failed to get user info: $error")
                return null
            }
            return convertToGoogleUserInfo(response.json().await<dynamic>())

            //This will not work here
            //return Json.decodeFromString<GoogleUser>(response.json().await<dynamic>())
        } catch (e: Exception) {
            Logger.e("Exception in getGoogleUserJsInfo", e)
            return null
        }
    }

    companion object {
        private const val TAG = "GoogleAuthManagerJs"
    }

}



