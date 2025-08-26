package com.sunildhiman90.kmauth.supabase

import co.touchlab.kermit.Logger
import com.sunildhiman90.kmauth.supabase.model.SupabaseExternalAuthConfig
import com.sunildhiman90.kmauth.supabase.model.SupabaseOAuthProvider
import com.sunildhiman90.kmauth.supabase.model.SupabaseUser
import com.sunildhiman90.kmauth.supabase.model.toSupabaseUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Implementation of Supabase authentication operations.
 */
class SupabaseAuthManager(
    private val redirectUrl: String? = null
) : CoroutineScope {


    private val supabaseClient: SupabaseClient by lazy {
        KMAuthSupabase.getSupabaseClient()
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job


    suspend fun signIn(
        provider: SupabaseOAuthProvider,
        config: () -> SupabaseExternalAuthConfig = { SupabaseExternalAuthConfig() }
    ) {
        signInCore(
            provider = provider,
            config = config,
        )
    }

    private fun signInCore(
        provider: SupabaseOAuthProvider,
        config: () -> SupabaseExternalAuthConfig = { SupabaseExternalAuthConfig() },
    ) {

        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            Logger.withTag("SupabaseAuthManager").e("Supabase signIn with $provider failed: $exception")
            KMAuthSupabase.updateSupabaseUserResult(Result.failure(Exception("Supabase signIn with $provider failed: $exception")))
        }

        launch(exceptionHandler) {
            try {

                if (redirectUrl != null) {
                    supabaseClient.auth.signInWith(
                        provider.toOAuthProvider(),
                        redirectUrl = this@SupabaseAuthManager.redirectUrl
                    ) {
                        config().toExternalAuthConfig()
                    }
                } else {
                    supabaseClient.auth.signInWith(provider.toOAuthProvider()) {
                        config().toExternalAuthConfig()
                    }
                }

            } catch (e: Exception) {
                Logger.withTag("SupabaseAuthManager").e("Supabase signIn with $provider failed: $e")
                KMAuthSupabase.updateSupabaseUserResult(Result.failure(Exception("Supabase signIn with $provider failed: $e")))
            }
        }
    }

    /**
     * Sign out the current user.
     */
    suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    /**
     * Get the current authenticated user, if any.
     */
    fun getCurrentUser(): SupabaseUser? {
        return supabaseClient.auth.currentUserOrNull()?.toSupabaseUser()
    }

    /**
     * Clean up resources.
     */
    fun dispose() {
        job.cancel()
    }

}
