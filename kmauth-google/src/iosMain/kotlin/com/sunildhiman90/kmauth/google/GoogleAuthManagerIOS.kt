package com.sunildhiman90.kmauth.google
import co.touchlab.kermit.Logger
import cocoapods.GoogleSignIn.GIDSignIn
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthPlatformContext
import com.sunildhiman90.kmauth.core.KMAuthUser
import com.sunildhiman90.kmauth.core.toThrowable
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIApplication


const val PROFILE_PIC_SIZE: ULong = 350u

@OptIn(ExperimentalForeignApi::class)
internal class GoogleAuthManagerIOS : GoogleAuthManager {

    override suspend fun signIn(onSignResult: (KMAuthUser?, Throwable?) -> Unit) {
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

                val user = result?.user
                val userId = user?.userID
                val idToken = user?.idToken
                val accessToken = user?.accessToken
                Logger.withTag(TAG).d { "signIn user: $user" }
                userId?.let {
                    kmAuthUser = KMAuthUser(
                        id = userId,
                        idToken = idToken?.tokenString,
                        accessToken = accessToken?.tokenString,
                        name = user.profile?.name,
                        email = user.profile?.email,
                        profilePicUrl = user.profile?.imageURLWithDimension(dimension = PROFILE_PIC_SIZE)?.absoluteString()
                    )
                    onSignResult(kmAuthUser, null)
                }

            }
        } catch (e: Exception) {
            Logger.withTag(TAG).e { "Exception in google signIn: $e" }
        }
    }

    override suspend fun signOut() {
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