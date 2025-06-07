package com.sunildhiman90.kmauth.google

import com.sunildhiman90.kmauth.core.KMAuthUser


interface GoogleAuthManager {

    /**
     * Sign in the user
     */
    suspend fun signIn(onSignResult: (KMAuthUser?, Throwable?) -> Unit)

    /**
     * Sign in the user without callback and return the KMAuthUser
     */
    suspend fun signIn() : Result<KMAuthUser?>

    /**
     * Sign out the user
     * @param userId will be needed for desktop(Jvm) platform Becoz it requires revoking the oauth2 token
     * and for that it requires userId
     */
    suspend fun signOut(userId: String? = null)

}