package com.sunildhiman90.kmauth.supabase

import co.touchlab.kermit.Logger
import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.supabase.KMAuthSupabase.initialize
import com.sunildhiman90.kmauth.supabase.deeplink.DeepLinkHandler
import com.sunildhiman90.kmauth.supabase.deeplink.getDeepLinkHandler
import com.sunildhiman90.kmauth.supabase.model.SupabaseExternalAuthConfig
import com.sunildhiman90.kmauth.supabase.model.SupabaseOAuthProvider
import com.sunildhiman90.kmauth.supabase.model.SupabaseUser
import com.sunildhiman90.kmauth.supabase.model.toSupabaseUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Main entry point for Supabase authentication.
 */
object KMAuthSupabase : CoroutineScope {

    private var authManager: SupabaseAuthManager? = null

    private lateinit var supabaseClient: SupabaseClient

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private var sessionJob: Job? = null

    private val _supabaseUser: MutableStateFlow<SupabaseUser?> = MutableStateFlow(null)
    val supabaseUser: StateFlow<SupabaseUser?> = _supabaseUser.asStateFlow()

    /**
     * Initialize the Supabase client with the given configuration only once.
     *
     * @param config The authentication configuration.
     * @param redirectUrl Optional redirect URL for OAuth callbacks.
     * @return The initialized [SupabaseAuthManager] instance.
     */
    fun initialize(
        config: KMAuthConfig,
        redirectUrl: String? = null
    ): SupabaseAuthManager {
        //TODO, apply mutex
        if (authManager != null) {
            return authManager!!
        }

        if (!::supabaseClient.isInitialized) {
            supabaseClient = createSupabaseClient(
                supabaseUrl = config.supabaseUrl ?: error("supabaseUrl is required in config"),
                supabaseKey = config.supabaseKey ?: error("supabaseKey is required in config")
            ) {
                install(Auth) {
                    // Configure auth settings for web
                    this.autoLoadFromStorage = config.autoLoadFromStorage
                    this.alwaysAutoRefresh = config.autoRefreshToken
                    redirectUrl?.let { this.defaultRedirectUrl = it }

                    //Android deep links
                    config.androidDeepLinkHost?.let { this.host = it }
                    config.androidDeepLinkScheme?.let { this.scheme = it }

                    //TODO, add iOS deep links url scheme
                }
            }
        }
        startCollectingSessionStatus()
        return SupabaseAuthManager(redirectUrl).also { authManager = it }
    }

    /**
     * Start collecting supabase session status updates.
     */
    fun startCollectingSessionStatus() {
        Logger.i("start Collecting Supabase sessionStatus")
        sessionJob?.cancel()
        sessionJob = launch {
            getSupabaseClient().auth.sessionStatus.collect { sessionStatus ->
                Logger.withTag("KMAuthSupabase").i("sessionStatus: $sessionStatus")
                val user: SupabaseUser? = when (sessionStatus) {
                    is SessionStatus.Initializing -> {
                        Logger.i("Initializing")
                        null
                    }

                    is SessionStatus.Authenticated -> {
                        Logger.i("Authenticated")
                        val supabaseUser = sessionStatus.session.user
                        supabaseUser?.toSupabaseUser()
                    }

                    else -> {
                        null
                    }
                }
                _supabaseUser.value = user
            }
        }
    }

    /**
     * Get the current instance of [SupabaseClient].
     *
     * @throws IllegalStateException if [initialize] has not been called.
     */
    fun getSupabaseClient(): SupabaseClient {
        return supabaseClient
    }

    /**
     * Get the current instance of [DeepLinkHandler].
     */
    fun deepLinkHandler(): DeepLinkHandler = getDeepLinkHandler()

    /**
     * Get the current instance of [SupabaseAuthManager].
     *
     * @throws IllegalStateException if [initialize] has not been called.
     */
    fun getAuthManager(): SupabaseAuthManager {
        return authManager
            ?: throw IllegalStateException("KMAuthSupabase not initialized. Call initialize() first.")
    }

    /**
     * Clean up resources.
     */
    fun dispose() {
        authManager = null
    }


    /**
     * Sign in with a provider using the default auth manager.
     */
    suspend fun signInWith(
        provider: SupabaseOAuthProvider,
        config: () -> SupabaseExternalAuthConfig = { SupabaseExternalAuthConfig() },
    ) {
        getAuthManager().signIn(provider, config)
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
}

