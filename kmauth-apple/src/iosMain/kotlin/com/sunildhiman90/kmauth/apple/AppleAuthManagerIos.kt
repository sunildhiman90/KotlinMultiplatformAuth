package com.sunildhiman90.kmauth.apple

import co.touchlab.kermit.Logger
import com.sunildhiman90.kmauth.core.KMAuthUser
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import kotlin.coroutines.resume

class AppleAuthManagerIOS : AppleAuthManager {
    private val TAG = "AppleAuthManagerIOS"
    private val appleIDProvider = ASAuthorizationAppleIDProvider()
    // Keep a strong reference to the delegate
    private var delegate: ASAuthorizationControllerDelegate? = null
    // Keep track of the current continuation
    private var currentContinuation: CancellableContinuation<Result<KMAuthUser?>>? = null

    override suspend fun signIn(): Result<KMAuthUser?> {
        Logger.withTag(TAG).i("Starting Apple Sign In flow")
        return suspendCancellableCoroutine { continuation ->
            currentContinuation = continuation
            
            // Set up cancellation
            continuation.invokeOnCancellation {
                Logger.withTag(TAG).i("Apple Sign In was cancelled")
                delegate = null
                currentContinuation = null
            }
            
            val onSignResult: (KMAuthUser?, Throwable?) -> Unit = { user, error ->
                Logger.withTag(TAG).i("Received sign in result - user: ${user?.id}, error: $error")
                try {
                    if (error == null) {
                        Logger.withTag(TAG).i("Sign in successful for user: ${user?.id}")
                        continuation.resume(Result.success(user))
                    } else {
                        Logger.withTag(TAG).e(error) { "Sign in failed" }
                        continuation.resume(Result.failure(error))
                    }
                } catch (e: Exception) {
                    Logger.withTag(TAG).e(e) { "Error in sign in continuation" }
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(e))
                    }
                } finally {
                    // Clean up
                    delegate = null
                    currentContinuation = null
                }
            }

            try {
                Logger.withTag(TAG).i("Creating Apple ID request")
                val request = appleIDProvider.createRequest().apply {
                    requestedScopes = listOf(ASAuthorizationScopeEmail, ASAuthorizationScopeFullName)
                    Logger.withTag(TAG).i("Request scopes: ${requestedScopes?.joinToString()}")
                }
                
                Logger.withTag(TAG).i("Creating and performing authorization request")
                delegate = ASAuthorizationControllerDelegate(onSignResult).also { delegate ->
                    Logger.withTag(TAG).i("Delegate created, performing request")
                    delegate.performRequest(request)
                }
            } catch (e: Exception) {
                Logger.withTag(TAG).e(e) { "Error creating sign in request" }
                onSignResult(null, e)
            }
        }
    }

    override suspend fun signOut(userId: String?) {
        Logger.withTag(TAG).i("signOut called for user: $userId")
        // Apple Sign In doesn't have a traditional sign out flow
        // Clear any local user data here if needed
    }
}
