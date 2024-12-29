package com.sunildhiman90.kmauth.google

import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthUser
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLScriptElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import kotlin.js.json

const val GSI_CLIENT_URL = "https://accounts.google.com/gsi/client"
const val GOOGLE_BUTTON_ID = "gid"

//For js global object variables, we can use external object
external object google {
    object accounts {
        object id {
            fun initialize(config: dynamic)
            fun renderButton(element: dynamic, options: dynamic)
            fun prompt(callback: (response: PromptMomentNotification) -> Unit)
            fun disableAutoSelect()

            object PromptMomentNotification {
                fun isDisplayMoment(): Boolean?
                fun isDisplayed(): Boolean?
                fun isNotDisplayed(): Boolean?
                fun getNotDisplayedReason(): String?
                fun isSkippedMoment(): Boolean?
                fun getSkippedReason(): Boolean?
                fun isDismissedMoment(): Boolean?
                fun getDismissedReason(): String?
                fun getMomentType(): String?
            }
        }
    }
}


internal class GoogleAuthManagerJs : GoogleAuthManager {

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
            console.log("Google sign in library loaded")
            isLibraryLoaded = true
            initializeGoogleSignIn(
                webClientId,
                onError = {
                    console.info("initializeGoogleSignIn: onError")
                    onSignResult?.invoke(null, Exception("Google sign in failed"))
                },
                onSuccess = { credential ->
                    console.info("initializeGoogleSignIn: onSuccess")
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
        onSuccess: (dynamic) -> Unit,
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

                val callbackFunction: (dynamic) -> Unit = { response: dynamic ->
                    console.log("initializeGoogleSignIn: callbackFunction")
                    isGoogleClientInitialized = true

                    // Get the credential (JWT token) from the response,
                    // credential:  this field is the ID token as a base64-encoded JSON Web Token (JWT) string
                    val credential = response.credential

                    if (credential != null) {
                        onSuccess(credential)
                    } else {
                        onError?.invoke()
                    }
                }

                google.accounts.id.initialize(
                    config = json(
                        "client_id" to clientId,
                        "ux_mode" to "popup",
                        "callback" to callbackFunction,
                        "use_fedcm_for_prompt" to true
                    )
                )

                addGoogleSignInButton()

                renderGoogleSignInButton(
                    containerId = GOOGLE_BUTTON_ID,
                    theme = "outline",
                    size = "large"
                )

            } catch (e: dynamic) {
                console.error("Google Sign-In initialization failed: $e")
                isGoogleClientInitialized = false
                onError?.invoke()
            }

        }

    }

    private fun promptGoogleSignIn() {
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

    private fun decodeJwtPayload(jwt: String): dynamic {
        // JWTs are Base64URL encoded. Split the token and decode the payload part.
        return try {
            val payload = jwt.split(".")[1] // The payload is the second part of the JWT
            val decodedPayload = window.atob(payload) // Decode Base64URL to a string
            JSON.parse<dynamic>(decodedPayload) // Parse JSON string to a dynamic object
        } catch (e: Exception) {
            console.error("Failed to decode JWT payload", e)
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
        private const val TAG = "GoogleAuthManagerJs"
    }

}



