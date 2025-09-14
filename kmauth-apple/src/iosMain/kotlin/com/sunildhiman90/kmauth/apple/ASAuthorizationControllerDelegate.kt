package com.sunildhiman90.kmauth.apple

import co.touchlab.kermit.Logger
import com.sunildhiman90.kmauth.core.KMAuthUser
import com.sunildhiman90.kmauth.core.toThrowable
import com.sunildhiman90.kmauth.supabase.KMAuthSupabase
import com.sunildhiman90.kmauth.supabase.model.SupabaseAuthConfig
import com.sunildhiman90.kmauth.supabase.model.SupabaseDefaultAuthProvider
import com.sunildhiman90.kmauth.supabase.model.SupabaseOAuthProvider
import com.sunildhiman90.kmauth.supabase.model.SupabaseUser
import com.sunildhiman90.kmauth.supabase.model.toKMAuthUser
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDRequest
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASPasswordCredential
import platform.AuthenticationServices.ASPresentationAnchor
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ASAuthorizationControllerDelegate(
    private val onSignResult: (KMAuthUser?, Throwable?) -> Unit
) : NSObject(),
    ASAuthorizationControllerDelegateProtocol,
    ASAuthorizationControllerPresentationContextProvidingProtocol {

    private val TAG = "ASAuthorizationControllerDelegate"
    private var controller: ASAuthorizationController? = null

    init {
        Logger.withTag(TAG).i("Delegate initialized")
    }

    fun performRequest(request: ASAuthorizationAppleIDRequest) {
        Logger.withTag(TAG).i("🔄 Performing authorization request")
        try {
            val controller = ASAuthorizationController(listOf(request)).apply {
                Logger.withTag(TAG).i("Setting up ASAuthorizationController")
                delegate = this@ASAuthorizationControllerDelegate
                presentationContextProvider = this@ASAuthorizationControllerDelegate
                Logger.withTag(TAG)
                    .i("Controller setup complete - delegate: $delegate, presentationProvider: $presentationContextProvider")
            }
            this.controller = controller
            Logger.withTag(TAG).i("Calling performRequests()")
            controller.performRequests()
            Logger.withTag(TAG).i("performRequests() called successfully")
        } catch (e: Exception) {
            Logger.withTag(TAG).e(e) { " Error in performRequest" }
            throw e
        }
    }

    // Authorization error callback
    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError
    ) {
        val errorMsg = " Apple Auth: Authorization failed with error: $didCompleteWithError"
        Logger.withTag(TAG).e { errorMsg }

        // Check if the error is a cancellation
        if (didCompleteWithError.domain == "com.apple.AuthenticationServices.AuthorizationError" &&
            didCompleteWithError.code.toInt() == 1001
        ) {
            Logger.withTag(TAG).i("👤 User cancelled the authorization flow")
        }

        onSignResult.invoke(null, didCompleteWithError.toThrowable())
    }

    // Authorization success callback
    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization
    ) {
        Logger.withTag(TAG).i("✅ Authorization completed successfully")
        Logger.withTag(TAG).i("🔑 Credential: ${didCompleteWithAuthorization.credential}")

        try {
            val credential = didCompleteWithAuthorization.credential
            when (credential) {
                is ASAuthorizationAppleIDCredential -> {
                    Logger.withTag(TAG).i("Processing ASAuthorizationAppleIDCredential")
                    handleAppleIDCredential(credential)
                }

                is ASPasswordCredential -> {
                    Logger.withTag(TAG).i("Credential is ASPasswordCredential")
                    // Handle password credential if needed
                }

                else -> {
                    val error =
                        Exception("Unexpected credential type: ${credential::class.simpleName}")
                    Logger.withTag(TAG).e(error) { "Unexpected credential type" }
                    onSignResult(null, error)
                }
            }
        } catch (e: Exception) {
            Logger.withTag(TAG).e(e) { "Error processing authorization" }
            onSignResult(null, e)
        }
    }

    @OptIn(BetaInteropApi::class)
    private fun handleAppleIDCredential(credential: ASAuthorizationAppleIDCredential) {
        Logger.withTag(TAG).i("Handling Apple ID credential for user: ${credential.user}")

        try {
            val idToken = credential.identityToken?.let { token ->
                //This will return the original identityToken
                NSString.create(
                    data = token,
                    encoding = NSUTF8StringEncoding
                )?.toString()

                //This will return the base64 encoded string of identityToken
                //token.base64EncodedStringWithOptions(NSDataBase64EncodingOptions.MAX_VALUE)
            } ?: run {
                val error = Exception("No identity token received")
                Logger.withTag(TAG).e(error) { "Missing identity token" }
                onSignResult(null, error)
                return
            }

            try {

                //NOTE: Instead of decoding token use supabase login here to get full user info
                val exceptionHandler = CoroutineExceptionHandler { _, exception ->
                    Logger.withTag(TAG).e(exception) { "Error in supabase signInWithIdToken: " }
                    onSignResult(null, exception)
                }
                val job = CoroutineScope(Dispatchers.Default + exceptionHandler).launch {
                    val supabaseUserResult: Result<SupabaseUser?> =
                        KMAuthSupabase.getAuthManager().signInWith(
                            supabaseDefaultAuthProvider = SupabaseDefaultAuthProvider.ID_TOKEN,
                            config = SupabaseAuthConfig().apply {
                                this.idToken = idToken
                                this.provider = SupabaseOAuthProvider.APPLE
                            }
                        )
                    if (supabaseUserResult.isSuccess) {
                        onSignResult(
                            supabaseUserResult.getOrNull()?.toKMAuthUser()?.copy(
                                idToken = idToken
                            ), null
                        )
                        cancel("Apple Sign In Successful")
                    } else {
                        Logger.withTag(TAG)
                            .e(supabaseUserResult.exceptionOrNull()) { "Error in supabase signInWithIdToken: " }
                        onSignResult(null, supabaseUserResult.exceptionOrNull())
                        cancel("Error in supabase signInWithIdToken: ${supabaseUserResult.exceptionOrNull()}")
                    }
                }

                job.invokeOnCompletion {
                    Logger.withTag(TAG).i("Apple Sign In job completed")
                    if (it is CancellationException) {
                        Logger.withTag(TAG)
                            .i("Apple Sign In job completed with CancellationException")
                    }
                }

            } catch (e: Exception) {
                Logger.withTag(TAG).e(e) { "Error in supabase signInWithIdToken: $e" }
                onSignResult(null, e)
            }

        } catch (e: Exception) {
            Logger.withTag(TAG).e(e) { "Error processing Apple ID credential: $e" }
            onSignResult(null, e)
        }
    }


    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): ASPresentationAnchor? {
        return UIApplication.sharedApplication.keyWindow
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decodeJwtPayload(jwt: String): JsonElement? {
        Logger.withTag(TAG).i("Decoding JWT payload: $jwt")
        return try {
            // Use Kotlin's Base64 class for cross-platform compatibility
            val payloadBase64Url = jwt.split(".")[1]

            // Base64.decode(payloadBase64Url).decodeToString() will not work here, becoz jwt payload does not contain padding, but base64 decode is expecting padding.

            // Create an optional padding Base64.UrlSafe decoder. PRESENT_OPTIONAL wil work with both padded or unpadded input:
            // https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.io.encoding/-base64/-padding-option/
            val base64UrlUnpadded =
                Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL)

            // Decode the payload.
            val decodedBytesString = base64UrlUnpadded.decode(payloadBase64Url).decodeToString()
            Json.parseToJsonElement(decodedBytesString)
        } catch (e: Exception) {
            Logger.e("Failed to decode JWT payload", e)
            null
        }
    }
}