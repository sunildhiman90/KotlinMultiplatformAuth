package com.sunildhiman90.kmauth.google

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthPlatformContext
import com.sunildhiman90.kmauth.core.KMAuthUser


internal class GoogleAuthManagerAndroid : GoogleAuthManager {

    private var credentialManager: CredentialManager
    private var kmAuthPlatformContext: KMAuthPlatformContext? = null
    private var webClientId: String
    private var context: Context

    init {

        kmAuthPlatformContext = KMAuthInitializer.getKMAuthPlatformContext()

        require(kmAuthPlatformContext?.context != null) {
            val message =
                "Android context should not be null, Please set it via kmAuthPlatformContext in KMAuthInitializer::init"
            co.touchlab.kermit.Logger.withTag(TAG).e(message)
            message
        }

        require(!KMAuthInitializer.getWebClientId().isNullOrEmpty()) {
            val message =
                "webClientId should not be null or empty, Please set it in KMAuthInitializer::init"
            co.touchlab.kermit.Logger.withTag(TAG).e(message)
            message
        }

        webClientId = KMAuthInitializer.getWebClientId()!!
        context = kmAuthPlatformContext!!.context
        credentialManager = CredentialManager.create(context)

    }

    override suspend fun signIn(onSignResult: (KMAuthUser?, Throwable?) -> Unit) {
        try {

            // For popup view, we can use GetSignInWithGoogleOption, Otherwise we can GetGoogleIdOption for bottom sheet
            // set setFilterByAuthorizedAccounts to false, Otherwise it will not show any account first time
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            /*val getSignInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()*/

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Use an activity-based context to avoid undefined system UI launching behavior.
            val result: GetCredentialResponse = credentialManager.getCredential(
                context = context,
                request = request
            )
            onSignResult(handleSignIn(result), null)
        } catch (e: Exception) {
            e.printStackTrace()
            co.touchlab.kermit.Logger.withTag(TAG).e("google signIn failed: $e")
            onSignResult(null, e)
        }

    }

    private fun handleSignIn(result: GetCredentialResponse): KMAuthUser? {
        return when (val credential = result.credential) {

            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract the ID to validate and
                        // authenticate on your server for more security.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        // TO make it more secure, For that you first need to validate the token:
                        // pass googleIdTokenCredential.getIdToken() to the backend server.
                        val idToken = googleIdTokenCredential.idToken
                        // To get a stable account identifier (e.g. for storing user data),
                        // use the subject ID:
                        KMAuthUser(
                            id = googleIdTokenCredential.id,
                            idToken = idToken,
                            name = googleIdTokenCredential.displayName,
                            profilePicUrl = googleIdTokenCredential.profilePictureUri.toString()
                        )
                    } catch (e: GoogleIdTokenParsingException) {
                        co.touchlab.kermit.Logger.withTag(TAG).e("Received an invalid google id token response: $e")
                        null
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    co.touchlab.kermit.Logger.withTag(TAG).e( "Unexpected type of credential")
                    null
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                co.touchlab.kermit.Logger.withTag(TAG).e("Unexpected type of credential")
                null
            }
        }
    }

    override suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            co.touchlab.kermit.Logger.withTag(TAG).e { "Exception in google signOut failed: $e" }
        }
    }

    companion object {
        private const val TAG = "GoogleAuthManagerAndroid"
    }
}