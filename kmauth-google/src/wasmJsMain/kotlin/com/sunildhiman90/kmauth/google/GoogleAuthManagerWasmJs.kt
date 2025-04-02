package com.sunildhiman90.kmauth.google

import co.touchlab.kermit.Logger
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthUser
import com.sunildhiman90.kmauth.google.externals.google
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLScriptElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import org.w3c.xhr.JSON


const val GSI_CLIENT_URL = "https://accounts.google.com/gsi/client"
const val GOOGLE_BUTTON_ID = "gid"

internal class GoogleAuthManagerWasmJs : GoogleAuthManager {

    private var isLibraryLoaded: Boolean = false
    private var isGoogleClientInitialized: Boolean = false
    private var webClientId: String

    private var onSignResult: ((KMAuthUser?, Throwable?) -> Unit)? = null

    init {

        require(!KMAuthInitializer.getWebClientId().isNullOrEmpty()) {
            val message =
                "webClientId should not be null or empty, Please set it in KMAuthInitializer::init"
            co.touchlab.kermit.Logger.withTag(TAG).e(message)
            message
        }

        webClientId = KMAuthInitializer.getWebClientId()!!

        loadGoogleSignInLibrary {
            Logger.d("Google sign in library loaded")
            isLibraryLoaded = true
            initializeGoogleSignIn(
                webClientId,
                onError = {
                    Logger.i("initializeGoogleSignIn: onError")
                    onSignResult?.invoke(null, Exception("Google sign in failed"))
                },
                onSuccess = { credential ->
                    Logger.i("initializeGoogleSignIn: onSuccess")
                    // Decode the JWT token to get the user's information
                    val userInfo = decodeJwtPayload(credential as String)

                    onSignResult?.invoke(
                        KMAuthUser(
                            id = userInfo.sub as String,
                            idToken = credential as? String,
                            name = userInfo.name as? String,
                            email = userInfo.email as? String,
                            profilePicUrl = userInfo.picture as? String
                        ), null
                    )
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
        onSuccess: (JsAny) -> Unit,
        onError: (() -> Unit)? = null
    ) {

        Logger.i("initializeGoogleSignIn")

        require(clientId.isNotEmpty()) {
            val error = "clientId should not be null or empty"
            Logger.d(error)
            error
        }

        check(isLibraryLoaded) {
            val error = "Google sign in library is not loaded"
            Logger.d(error)
            error
        }

        if (isLibraryLoaded) {

            try {

                val callbackFunction: (JsAny) -> Unit = { response: JsAny ->
                    Logger.d("initializeGoogleSignIn: callbackFunction")
                    isGoogleClientInitialized = true

                    // Get the credential (JWT token) from the response,
                    // credential:  this field is the ID token as a base64-encoded JSON Web Token (JWT) string
                    val credential = response.credential

                    if (credential != null) {
                        onSuccess(credential as JsAny)
                    } else {
                        onError?.invoke()
                    }
                }
                val config = buildJsonObject {
                    put("client_id", clientId)
                    put("ux_mode", "popup")
                    put("callback", callbackFunction as JsonElement)
                    put("use_fedcm_for_prompt", true)
                }

                google.accounts.id.initialize(
                    config = config
                )

                addGoogleSignInButton()

                renderGoogleSignInButton(
                    containerId = GOOGLE_BUTTON_ID,
                    theme = "outline",
                    size = "large"
                )

            } catch (e: Exception) {
                Logger.e("Google Sign-In initialization failed: $e")
                isGoogleClientInitialized = false
                onError?.invoke()
            }

        }

    }

    private fun promptGoogleSignIn() {
        Logger.d("promptGoogleSignIn:")
        google.accounts.id.prompt { notification: google.accounts.id.PromptMomentNotification ->
            if (notification.isSkippedMoment() == true) {
                Logger.i("promptGoogleSignIn one tap isNotDisplayed_or_isSkipped:")
                //trigger rendered button click
                document.getElementById(GOOGLE_BUTTON_ID)
                    ?.querySelector("div[role='button']")
                    ?.dispatchEvent(
                        Event("click")
                    )
            }
            if (notification.isDismissedMoment() == true) {
                //if "credential_returned", it means user has already signed in successfully
                Logger.i("promptGoogleSignIn callback dismissed_reason: ${notification.getDismissedReason()}")
            }
        }
    }

    private fun addGoogleSignInButton() {

        var alreadyAdded = false
        val existingScripts = document.body?.getElementsByClassName(GOOGLE_BUTTON_ID)
        val nodesLength = existingScripts?.length ?: 0
        for (i in 0 until nodesLength) {
            if ((existingScripts?.get(i) as HTMLScriptElement).className == GOOGLE_BUTTON_ID) {
                alreadyAdded = true
            }
        }

        if (alreadyAdded) return
        val gIdOnloadDiv = document.createElement("div") as HTMLElement
        gIdOnloadDiv.style.display = "none" //for hiding it from page
        gIdOnloadDiv.id = GOOGLE_BUTTON_ID
        gIdOnloadDiv.className = GOOGLE_BUTTON_ID
        document.body?.appendChild(gIdOnloadDiv)
    }

    private fun renderGoogleSignInButton(
        containerId: String,
        theme: String = "outline",
        size: String = "large",
    ) {
        val signInButtonWrapper = document.getElementById(containerId)
        google.accounts.id.renderButton(
            signInButtonWrapper,
            json(
                "theme" to theme,
                "size" to size
            )
        )
    }

    private fun decodeJwtPayload(jwt: String): JsAny {
        // JWTs are Base64URL encoded. Split the token and decode the payload part.
        return try {
            val payload = jwt.split(".")[1] // The payload is the second part of the JWT
            val decodedPayload = window.atob(payload) // Decode Base64URL to a string
            JSON.parse<JsAny>(decodedPayload) // Parse JSON string to a JsAny object
        } catch (e: Exception) {
            Logger.e("Failed to decode JWT payload", e)
            null
        }
    }

    override suspend fun signIn(onSignResult: (KMAuthUser?, Throwable?) -> Unit) {
        this.onSignResult = onSignResult
        promptGoogleSignIn()
    }

    override suspend fun signOut(userId: String?) {
        google.accounts.id.disableAutoSelect()
    }

    companion object {
        private const val TAG = "GoogleAuthManagerWasmJs"
    }

}

