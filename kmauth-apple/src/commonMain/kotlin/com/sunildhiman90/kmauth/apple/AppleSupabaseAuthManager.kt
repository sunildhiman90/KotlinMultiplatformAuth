package com.sunildhiman90.kmauth.apple

import com.sunildhiman90.kmauth.core.KMAuthUser
import com.sunildhiman90.kmauth.supabase.KMAuthSupabase
import com.sunildhiman90.kmauth.supabase.model.SupabaseOAuthProvider
import com.sunildhiman90.kmauth.supabase.model.SupabaseUser
import com.sunildhiman90.kmauth.supabase.model.toKMAuthUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Common implementation of Apple authentication using Supabase.
 * Platform-specific implementations should delegate to this class for jvm, android, js and wasmJs.
 */
internal class AppleSupabaseAuthManager : AppleAuthManager {
    private val authScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override suspend fun signIn(): Result<KMAuthUser?> {
        return try {
            val supabaseUserResult = KMAuthSupabase.getAuthManager().signInWith(
                supabaseOAuthProvider = SupabaseOAuthProvider.APPLE,
            )
            if (supabaseUserResult.isSuccess) {
                Result.success(supabaseUserResult.getOrNull()?.toKMAuthUser())
            } else {
                Result.failure(
                    supabaseUserResult.exceptionOrNull() ?: Exception("Some error in Apple Sign In")
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Some error in Apple Sign In: $e"))
        }
    }

    override suspend fun signOut(userId: String?) {
        KMAuthSupabase.signOut()
    }

    fun dispose() {
        authScope.cancel()
    }
}
