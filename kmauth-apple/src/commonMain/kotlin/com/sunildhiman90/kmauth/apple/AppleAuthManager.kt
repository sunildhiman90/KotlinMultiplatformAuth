package com.sunildhiman90.kmauth.apple

import com.sunildhiman90.kmauth.core.KMAuthUser

/**
 * Apple Auth Manager interface
 *
 * @property providerId String
 */
interface AppleAuthManager {

    private val providerId: String
        get() = "apple"


    suspend fun signIn(): Result<KMAuthUser?>

    suspend fun signOut()
}
