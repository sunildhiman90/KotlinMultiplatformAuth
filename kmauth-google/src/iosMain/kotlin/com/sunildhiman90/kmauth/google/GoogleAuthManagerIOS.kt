package com.sunildhiman90.kmauth.google
import co.touchlab.kermit.Logger
import cocoapods.GoogleSignIn.GIDSignIn
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthPlatformContext
import com.sunildhiman90.kmauth.core.KMAuthUser
import com.sunildhiman90.kmauth.core.toThrowable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.UIKit.UIApplication
import kotlin.coroutines.resume


const val PROFILE_PIC_SIZE: ULong = 350u

@OptIn(ExperimentalForeignApi::class)
internal class GoogleAuthManagerIOS : GoogleAuthManager {

    override suspend fun signIn(onSignResult: (KMAuthUser?, Throwable?) -> Unit) {
        signInCore(onSignResult)
    }

    private fun signInCore(onSignResult: (KMAuthUser?, Throwable?) -> Unit) {
        try {
            val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
            requireNotNull(rootViewController) { "Root view controller is null" }

            var kmAuthUser: KMAuthUser? = null
            GIDSignIn.sharedInstance.signInWithPresentingViewController(
                presentingViewController = rootViewController
            ) { result, error ->

                if (error != null) {
                    Logger.withTag(TAG).e { "Error in google Sign In: $error" }
                    onSignResult(null, error.toThrowable())
                    return@signInWithPresentingViewController
                }

                Logger.withTag(TAG).e { "Success in google Sign In" }

                val user = result?.user
                val userId = user?.userID
                val idToken = user?.idToken
                val accessToken = user?.accessToken
                userId?.let {
                    kmAuthUser = KMAuthUser(
                        id = userId,
                        idToken = idToken?.tokenString,
                        accessToken = accessToken?.tokenString,
                        name = user.profile?.name,
                        email = user.profile?.email,
                        profilePicUrl = user.profile?.imageURLWithDimension(dimension = PROFILE_PIC_SIZE)
                            ?.absoluteString()
                    )
                    onSignResult(kmAuthUser, null)
                }

            }
        } catch (e: Exception) {
            Logger.withTag(TAG).e { "Exception in google signIn: $e" }
        }
    }

    override suspend fun signIn(): Result<KMAuthUser?> {
        return suspendCancellableCoroutine { continuation ->
            val onSignResult: (KMAuthUser?, Throwable?) -> Unit = { user, error ->
                if (error == null) {
                    // Resume coroutine with an exception provided by the callback
                    continuation.resume(Result.success(user))
                } else {
                    // Resume coroutine with a value provided by the callback
                    continuation.resume(Result.failure(Exception("Error in google Sign In: $error")))
                }
            }
            signInCore(onSignResult)
        }
    }

    override suspend fun signOut(userId: String?) {
        try {
            GIDSignIn.sharedInstance.signOut()
        } catch (e: Exception) {
            Logger.withTag(TAG).e { "Exception in google signOut failed: $e" }
        }
    }

    companion object {
        private const val TAG = "GoogleAuthManagerIOS"
    }
}