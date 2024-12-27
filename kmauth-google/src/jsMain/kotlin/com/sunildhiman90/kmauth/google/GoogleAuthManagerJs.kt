package com.sunildhiman90.kmauth.google
import com.sunildhiman90.kmauth.core.KMAuthUser


internal class GoogleAuthManagerJs : GoogleAuthManager {


    override suspend fun signIn(onSignResult: (KMAuthUser?, Throwable?) -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun signOut() {
        TODO("Not yet implemented")
    }

    companion object {
        private const val TAG = "GoogleAuthManagerJs"
    }
}