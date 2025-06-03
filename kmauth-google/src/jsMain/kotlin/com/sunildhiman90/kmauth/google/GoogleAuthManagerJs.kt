package com.sunildhiman90.kmauth.google

import co.touchlab.kermit.Logger
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthUser
import com.sunildhiman90.kmauth.google.externals.CredentialResponse
import com.sunildhiman90.kmauth.google.externals.google
import com.sunildhiman90.kmauth.google.jsUtils.googleIdConfig
import com.sunildhiman90.kmauth.google.jsUtils.gsiButtonConfig
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLScriptElement
import org.w3c.dom.events.Event
import org.w3c.dom.get

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
                    Logger.i("initializeGoogleSignIn: onSuccess")
                    // Decode the JWT token to get the user's information
                    val userInfo = decodeJwtPayload(credential)?.jsonObject

                    if(userInfo != null) {
                        val sub = userInfo["sub"]?.jsonPrimitive
                        val name = userInfo["name"]?.jsonPrimitive
                        val email = userInfo["email"]?.jsonPrimitive
                        val picture = userInfo["picture"]?.jsonPrimitive

                        Logger.d("initializeGoogleSignIn: onSuccess: sub: $sub, name: $name, email: $email, picture: $picture")
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

                val callbackFunction: (CredentialResponse) -> Unit = { response ->
                    Logger.d("initializeGoogleSignIn: callbackFunction")
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
        val signInButtonWrapper = document.getElementById(containerId) as HTMLElement
        google.accounts.id.renderButton(
            signInButtonWrapper,
            gsiButtonConfig(theme, size)
        )
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



