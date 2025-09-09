package com.sunildhiman90.kmauth.supabase

import co.touchlab.kermit.Logger
import com.sunildhiman90.kmauth.supabase.model.PhoneConfirmationChannel.Companion.toSupabaseChannel
import com.sunildhiman90.kmauth.supabase.model.SupabaseAuthConfig
import com.sunildhiman90.kmauth.supabase.model.SupabaseDefaultAuthProvider
import com.sunildhiman90.kmauth.supabase.model.SupabaseOAuthProvider
import com.sunildhiman90.kmauth.supabase.model.SupabaseUser
import com.sunildhiman90.kmauth.supabase.model.toSupabaseUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.ExternalAuthConfig
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.builtin.Phone
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.JsonObject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

/**
 * Implementation of Supabase authentication operations.
 * It provides methods to sign in with a supabase [io.github.jan.supabase.auth.providers.OAuthProvider] provider or a supabase [io.github.jan.supabase.auth.providers.builtin.DefaultAuthProvider].
 *
 * @property redirectUrl: String? The redirect URL to use for the authentication flow.
 *
 * @see [SupabaseOAuthProvider]
 * @see [SupabaseDefaultAuthProvider]
 * @see [SupabaseAuthConfig]
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

    private var continuation: CancellableContinuation<Result<SupabaseUser?>>? = null
    private var sessionJob: Job? = null

    /**
     * Sign in with a supabase [io.github.jan.supabase.auth.providers.OAuthProvider] provider.
     *
     * @param supabaseOAuthProvider: [SupabaseOAuthProvider] The OAuth provider to sign in with.
     * @param config: [SupabaseAuthConfig] The configuration for the sign in operation.
     * @return A [Result] containing the result of the sign in operation of type [SupabaseUser].
     */
    suspend fun signInWith(
        supabaseOAuthProvider: SupabaseOAuthProvider,
        config: SupabaseAuthConfig = SupabaseAuthConfig(),
    ): Result<SupabaseUser?> = suspendCancellableCoroutine { continuation ->
        this.continuation = continuation

        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            Logger.withTag("SupabaseAuthManager")
                .e("Supabase signIn with $supabaseOAuthProvider failed: $exception")
            KMAuthSupabase.updateSupabaseUserResult(Result.failure(Exception("Supabase signIn with $supabaseOAuthProvider failed: $exception")))
            this@SupabaseAuthManager.continuation?.resume(Result.failure(Exception("Supabase signIn with $supabaseOAuthProvider failed: $exception")))
            sessionJob?.cancel("SupabaseAuthManager Authentication failed exceptionHandler: $exception")
        }

        launch(exceptionHandler) {
            try {
                val oauthProvider = supabaseOAuthProvider.toOAuthProvider()
                val externalAuthConfigBuilder: ExternalAuthConfig.() -> Unit = {
                    scopes.addAll(config.scopes)
                    queryParams.putAll(config.queryParams)
                    automaticallyOpenUrl = config.automaticallyOpenUrl
                    // Add supabaseUrl as audience if provided
                    config.supabaseUrl?.let { url ->
                        queryParams["audience"] = url
                    }
                }

                if (redirectUrl != null) {
                    supabaseClient.auth.signInWith(
                        provider = oauthProvider,
                        redirectUrl = this@SupabaseAuthManager.redirectUrl,
                        config = externalAuthConfigBuilder
                    )
                } else {
                    supabaseClient.auth.signInWith(
                        provider = oauthProvider,
                        config = externalAuthConfigBuilder
                    )
                }

                sessionJob?.cancel()
                sessionJob = launch(exceptionHandler) {
                    supabaseClient.auth.sessionStatus.collect { sessionStatus ->
                        Logger.withTag("SupabaseAuthManager").i("sessionStatus: $sessionStatus")
                        when (sessionStatus) {
                            is SessionStatus.Initializing -> {
                                Logger.i("Initializing")
                            }

                            is SessionStatus.Authenticated -> {
                                Logger.i("Authenticated")
                                val supabaseUser = sessionStatus.session.user
                                val accessToken = sessionStatus.session.accessToken
                                val user =
                                    supabaseUser?.toSupabaseUser()?.copy(accessToken = accessToken)
                                this@SupabaseAuthManager.continuation?.resume(Result.success(user))
                                // cancel the sessionJob
                                cancel("Authentication completed")
                            }

                            else -> {
                                Logger.i("SupabaseAuthManager sessionStatus: $sessionStatus")
                            }
                        }
                    }
                }.apply {
                    invokeOnCompletion { cause ->
                        Logger.withTag("SupabaseAuthManager")
                            .d("Session collection completed: ${cause?.message ?: "Completed"}")
                        if (cause is CancellationException) {
                            Logger.withTag("SupabaseAuthManager")
                                .d("Session collection was cancelled: ${cause.message}")
                        }
                        sessionJob = null
                    }
                }
            } catch (e: Exception) {
                Logger.withTag("SupabaseAuthManager")
                    .e("Supabase signIn with $supabaseOAuthProvider failed: $e")
                KMAuthSupabase.updateSupabaseUserResult(Result.failure(Exception("Supabase signIn with $supabaseOAuthProvider failed: $e")))
                this@SupabaseAuthManager.continuation?.resume(Result.failure(Exception("Supabase signIn with $supabaseOAuthProvider failed: $e")))
                sessionJob?.cancel("SupabaseAuthManager Authentication failed: $e")

            }
        }
    }

    /**
     * Sign in with a supabase [io.github.jan.supabase.auth.providers.builtin.DefaultAuthProvider].
     * It supports only default supabase providers such as [Email], [Phone] and [IDToken].
     *
     * @param supabaseDefaultAuthProvider: [SupabaseDefaultAuthProvider] The default auth provider to sign in with.
     * @param config: [SupabaseAuthConfig] The configuration for the sign in operation.
     * @return A [Result] containing the result of the sign in operation of type [SupabaseUser].
     */
    suspend fun signInWith(
        supabaseDefaultAuthProvider: SupabaseDefaultAuthProvider,
        config: SupabaseAuthConfig = SupabaseAuthConfig(),
    ): Result<SupabaseUser?> = suspendCancellableCoroutine { continuation ->
        this.continuation = continuation

        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            Logger.withTag("SupabaseAuthManager")
                .e("Supabase signIn with $supabaseDefaultAuthProvider failed: $exception")
            KMAuthSupabase.updateSupabaseUserResult(Result.failure(Exception("Supabase signIn with $supabaseDefaultAuthProvider failed: $exception")))
            this@SupabaseAuthManager.continuation?.resume(Result.failure(Exception("Supabase signIn with $supabaseDefaultAuthProvider failed: $exception")))
            sessionJob?.cancel("SupabaseAuthManager Authentication failed exceptionHandler: $exception")
        }

        launch(exceptionHandler) {
            try {
                // Define configuration blocks for each provider type
                val emailConfig: Email.Config.() -> Unit = {
                    email = config.email
                    password = config.password
                }

                val idTokenConfig: IDToken.Config.() -> Unit = {
                    idToken = config.idToken
                    provider = config.provider?.toIdTokenProvider()
                    accessToken = config.accessToken
                    nonce = config.nonce
                }

                val phoneConfig: Phone.Config.() -> Unit = {
                    phone = config.phone
                    password = config.password
                    channel = config.channel.toSupabaseChannel()
                }

                // Execute the appropriate sign-in with the pre-configured blocks
                when (supabaseDefaultAuthProvider) {
                    SupabaseDefaultAuthProvider.EMAIL -> {
                        if (redirectUrl != null) {
                            supabaseClient.auth.signInWith(
                                Email,
                                redirectUrl = redirectUrl,
                                config = emailConfig
                            )
                        } else {
                            supabaseClient.auth.signInWith(Email, config = emailConfig)
                        }
                    }

                    SupabaseDefaultAuthProvider.ID_TOKEN -> {
                        if (redirectUrl != null) {
                            supabaseClient.auth.signInWith(
                                IDToken,
                                redirectUrl = redirectUrl,
                                config = idTokenConfig
                            )
                        } else {
                            supabaseClient.auth.signInWith(IDToken, config = idTokenConfig)
                        }
                    }

                    SupabaseDefaultAuthProvider.PHONE -> {
                        if (redirectUrl != null) {
                            supabaseClient.auth.signInWith(
                                Phone,
                                redirectUrl = redirectUrl,
                                config = phoneConfig
                            )
                        } else {
                            supabaseClient.auth.signInWith(Phone, config = phoneConfig)
                        }
                    }
                }

                sessionJob?.cancel()
                sessionJob = launch(exceptionHandler) {
                    supabaseClient.auth.sessionStatus.collect { sessionStatus ->
                        Logger.withTag("SupabaseAuthManager").i("sessionStatus: $sessionStatus")
                        when (sessionStatus) {
                            is SessionStatus.Initializing -> {
                                Logger.i("Initializing")
                            }

                            is SessionStatus.Authenticated -> {
                                Logger.i("Authenticated")
                                val supabaseUser = sessionStatus.session.user
                                val accessToken = sessionStatus.session.accessToken
                                val user =
                                    supabaseUser?.toSupabaseUser()?.copy(accessToken = accessToken)
                                this@SupabaseAuthManager.continuation?.resume(Result.success(user))
                                // cancel the sessionJob
                                cancel("Authentication completed")
                            }

                            else -> {
                                Logger.i("SupabaseAuthManager sessionStatus: $sessionStatus")
                            }
                        }
                    }
                }.apply {
                    invokeOnCompletion { cause ->
                        Logger.withTag("SupabaseAuthManager")
                            .d("Session collection completed: ${cause?.message ?: "Completed"}")
                        if (cause is CancellationException) {
                            Logger.withTag("SupabaseAuthManager")
                                .d("Session collection was cancelled: ${cause.message}")
                        }
                        sessionJob = null
                    }
                }
            } catch (e: Exception) {
                Logger.withTag("SupabaseAuthManager")
                    .e("Supabase signIn with $supabaseDefaultAuthProvider failed: $e")
                KMAuthSupabase.updateSupabaseUserResult(Result.failure(Exception("Supabase signIn with $supabaseDefaultAuthProvider failed: $e")))
                this@SupabaseAuthManager.continuation?.resume(Result.failure(Exception("Supabase signIn with $supabaseDefaultAuthProvider failed: $e")))
                sessionJob?.cancel("SupabaseAuthManager Authentication failed: $e")

            }
        }
    }

    /**
     * Reset password for a user using their email.
     */
    suspend fun resetPasswordForEmail(
        email: String,
    ) {
        supabaseClient.auth.resetPasswordForEmail(email)
    }

    /**
     * Links an OAuth Identity to an existing user.
     * The user needs to be signed in to call this method.
     * If the candidate identity is already linked to the existing user or another user, this will fail.
     *
     * @param supabaseOAuthProvider The OAuth provider to link.
     */
    suspend fun linkIdentity(
        supabaseOAuthProvider: SupabaseOAuthProvider
    ) {
        supabaseClient.auth.linkIdentity(supabaseOAuthProvider.toOAuthProvider())
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
