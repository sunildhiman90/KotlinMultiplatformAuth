package com.sunildhiman90.kmauth.google

import com.sunildhiman90.kmauth.core.KMAuthUser


interface GoogleAuthManager {

    /**
     * Sign in the user
     */
    suspend fun signIn(onSignResult: (KMAuthUser?, Throwable?) -> Unit)

    /**
     * Sign out the user
     * @param userId will be needed for desktop(Jvm) platform Becoz it requires revoking the oauth2 token
     * and for that it requires userId
     */
    suspend fun signOut(userId: String? = null)

}