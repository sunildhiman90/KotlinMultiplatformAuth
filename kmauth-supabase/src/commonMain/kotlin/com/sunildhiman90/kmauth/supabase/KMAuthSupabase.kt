package com.sunildhiman90.kmauth.supabase

import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.supabase.model.SupabaseUser
import io.github.jan.supabase.auth.providers.OAuthProvider

/**
 * Main entry point for Supabase authentication.
 */
object KMAuthSupabase {
    
    private var authManager: SupabaseAuthManager? = null
    
    /**
     * Initialize the Supabase client with the given configuration.
     * 
     * @param config The authentication configuration.
     * @param redirectUrl Optional redirect URL for OAuth callbacks.
     * @return The initialized [SupabaseAuthManager] instance.
     */
    fun initialize(
        config: KMAuthConfig,
        redirectUrl: String = "http://localhost:8080/auth/callback"
    ): SupabaseAuthManager {
        return SupabaseAuthManager(config, redirectUrl).also { authManager = it }
    }
    
    /**
     * Get the current instance of [SupabaseAuthManager].
     * 
     * @throws IllegalStateException if [initialize] has not been called.
     */
    fun getAuthManager(): SupabaseAuthManager {
        return authManager ?: throw IllegalStateException("KMAuthSupabase not initialized. Call initialize() first.")
    }
    
    /**
     * Clean up resources.
     */
    fun dispose() {
        authManager?.dispose()
        authManager = null
    }


    /**
     * Extension function to sign in with a provider using the default auth manager.
     */
    suspend fun signInWith(
        provider: OAuthProvider,
        onSignResult: (SupabaseUser?, Throwable?) -> Unit
    ) {
        getAuthManager().signIn(provider, onSignResult)
    }

    /**
     * Extension function to sign in with a provider using the default auth manager.
     */
    suspend fun signInWith(provider: OAuthProvider): Result<SupabaseUser?> {
        return getAuthManager().signIn(provider)
    }

    /**
     * Extension function to sign out using the default auth manager.
     */
    suspend fun signOut() {
        getAuthManager().signOut()
    }

    /**
     * Extension function to get the current user using the default auth manager.
     */
    suspend fun getCurrentUser(): SupabaseUser? {
        return getAuthManager().getCurrentUser()
    }

    /**
     * Extension function to get the current session using the default auth manager.
     */
    suspend fun getCurrentSession(): String? {
        return getAuthManager().getCurrentSession()
    }
}

