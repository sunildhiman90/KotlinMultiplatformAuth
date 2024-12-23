package com.sunildhiman90.kmauth.google

import com.sunildhiman90.kmauth.core.KMAuthUser


interface GoogleAuthManager {
    suspend fun signIn(): KMAuthUser?
    suspend fun signOut()
}