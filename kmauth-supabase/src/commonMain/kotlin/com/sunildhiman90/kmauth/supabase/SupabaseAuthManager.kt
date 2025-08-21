package com.sunildhiman90.kmauth.supabase

import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.supabase.model.SupabaseExternalAuthConfig
import com.sunildhiman90.kmauth.supabase.model.SupabaseUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import com.sunildhiman90.kmauth.supabase.model.SupabaseOAuthProvider
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime

/**
 * Implementation of Supabase authentication operations.
 */
class SupabaseAuthManager(
    private val config: KMAuthConfig,
    private val redirectUrl: String? = null
) : CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = config.supabaseUrl ?: error("supabaseUrl is required in config"),
            supabaseKey = config.supabaseKey ?: error("supabaseKey is required in config")
        ) {
            install(Auth) {
                // Configure auth settings for web
                this.autoLoadFromStorage = config.autoLoadFromStorage
                this.alwaysAutoRefresh = config.autoRefreshToken
                redirectUrl?.let { this.defaultRedirectUrl = it }
            }
        }
    }

    /**
     * Sign in the user with the specified OAuth provider.
     *
     * @param provider The OAuth provider to use for sign-in.
     * @param onSignResult Callback with the authentication result.
     * @param config Additional configuration for the sign-in process.
     */
    suspend fun signIn(
        provider: SupabaseOAuthProvider,
        onSignResult: (SupabaseUser?, Throwable?) -> Unit,
        config: () -> SupabaseExternalAuthConfig = { SupabaseExternalAuthConfig() }
    ) {
        launch {
            try {
                val result = signIn(provider, config)
                onSignResult(result.getOrNull(), null)
            } catch (e: Exception) {
                onSignResult(null, e)
            }
        }
    }

    /**
     * Sign in the user with the specified OAuth provider.
     *
     * @param provider The OAuth provider to use for sign-in.
     * @return A [Result] containing the authenticated user or an exception.
     */

    suspend fun signIn(
        provider: SupabaseOAuthProvider,
        config: () -> SupabaseExternalAuthConfig = { SupabaseExternalAuthConfig() }
    ): Result<SupabaseUser?> = runCatching {
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

        // Get the current session to verify authentication
        val session = supabaseClient.auth.currentSessionOrNull()
        if (session != null) {
            val user = supabaseClient.auth.currentUserOrNull()
            user?.toSupabaseUser()
        } else {
            null
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
     * Get the current session token, if any.
     */
    suspend fun getCurrentSession(): String? {
        return supabaseClient.auth.currentSessionOrNull()?.accessToken
    }

    /**
     * Clean up resources.
     */
    fun dispose() {
        job.cancel()
    }

    /**
     * Converts a Supabase UserInfo object to a SupabaseUser.
     */
    @OptIn(ExperimentalTime::class)
    private fun io.github.jan.supabase.auth.user.UserInfo.toSupabaseUser(): SupabaseUser {
        return SupabaseUser(
            id = this.id,
            email = this.email,
            phone = this.phone,
            name = this.userMetadata?.get("full_name")?.jsonPrimitive?.content,
            firstName = this.userMetadata?.get("first_name")?.jsonPrimitive?.content,
            lastName = this.userMetadata?.get("last_name")?.jsonPrimitive?.content,
            avatarUrl = this.userMetadata?.get("avatar_url")?.jsonPrimitive?.content,
            provider = this.appMetadata?.get("provider")?.jsonPrimitive?.content,
            emailConfirmedAt = this.emailConfirmedAt?.toString(),
            phoneConfirmedAt = this.phoneConfirmedAt?.toString(),
            lastSignInAt = this.lastSignInAt?.toString(),
            createdAt = this.createdAt?.toString(),
            updatedAt = this.updatedAt?.toString()
        )
    }
}
