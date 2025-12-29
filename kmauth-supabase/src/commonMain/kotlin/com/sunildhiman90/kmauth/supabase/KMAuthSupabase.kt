package com.sunildhiman90.kmauth.supabase

import co.touchlab.kermit.Logger
import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.supabase.KMAuthSupabase.initialize
import com.sunildhiman90.kmauth.supabase.deeplink.DeepLinkHandler
import com.sunildhiman90.kmauth.supabase.deeplink.getDeepLinkHandler
import com.sunildhiman90.kmauth.supabase.model.SupabaseAuthConfig
import com.sunildhiman90.kmauth.supabase.model.SupabaseDefaultAuthProvider
import com.sunildhiman90.kmauth.supabase.model.SupabaseOAuthProvider
import com.sunildhiman90.kmauth.supabase.model.SupabaseUser
import com.sunildhiman90.kmauth.supabase.model.toSupabaseUser
import com.sunildhiman90.kmauth.supabase.utils.toFlowType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
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

    private val _supabaseUserResult: MutableStateFlow<Result<SupabaseUser?>> = MutableStateFlow(Result.success(null))
    val supabaseUserResult: StateFlow<Result<SupabaseUser?>> = _supabaseUserResult.asStateFlow()

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
                    flowType = config.flowType?.toFlowType() ?: FlowType.IMPLICIT

                    //Android and ios deep links
                    config.deepLinkHost?.let { this.host = it }
                    config.deepLinkScheme?.let { this.scheme = it }
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
                when (sessionStatus) {
                    is SessionStatus.Initializing -> {
                        Logger.i("Initializing")
                    }

                    is SessionStatus.Authenticated -> {
                        Logger.i("Authenticated")
                        val supabaseUser = sessionStatus.session.user
                        val accessToken = sessionStatus.session.accessToken
                        val user = supabaseUser?.toSupabaseUser()?.copy(accessToken = accessToken)
                        _supabaseUserResult.value = Result.success(user)
                    }

                    else -> {
                        Logger.i("supabase sessionStatus: $sessionStatus")
                    }
                }
            }
        }
    }

    fun updateSupabaseUserResult(result: Result<SupabaseUser?>) {
        _supabaseUserResult.value = result
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
        supabaseOAuthProvider: SupabaseOAuthProvider,
        config: SupabaseAuthConfig = SupabaseAuthConfig(),
    ) {
        getAuthManager().signInWith(supabaseOAuthProvider, config)
    }

    /**
     * Sign in with a default auth provider using the default auth manager.
     */
    suspend fun signInWithDefaultAuthProvider(
        supabaseDefaultAuthProvider: SupabaseDefaultAuthProvider,
        config: SupabaseAuthConfig = SupabaseAuthConfig(),
    ) {
        getAuthManager().signInWith(supabaseDefaultAuthProvider, config)
    }

    /**
     * Sign up with a default auth provider using the default auth manager.
     * Supports Email and Phone providers for creating new user accounts.
     */
    suspend fun signUpWithDefaultAuthProvider(
        supabaseDefaultAuthProvider: SupabaseDefaultAuthProvider,
        config: SupabaseAuthConfig = SupabaseAuthConfig(),
    ) {
        getAuthManager().signUpWith(supabaseDefaultAuthProvider, config)
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

