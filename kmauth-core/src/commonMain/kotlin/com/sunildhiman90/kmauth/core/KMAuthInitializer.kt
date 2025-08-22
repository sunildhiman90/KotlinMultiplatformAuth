package com.sunildhiman90.kmauth.core

/**
 * This class is used for initialization of KotlinMultiplatformAuth.
 */
object KMAuthInitializer {

    private var config: KMAuthConfig? = null
    private var kmAuthPlatformContext: KMAuthPlatformContext? = null

    /**
     * Initializes the authentication system with the provided configuration.
     *
     * @param config The authentication configuration.
     */
    fun initialize(config: KMAuthConfig) {
        this.config = config
        this.kmAuthPlatformContext = config.context as? KMAuthPlatformContext
    }

    /**
     * Initializes the authentication system with a platform context.
     * This is a convenience method for backward compatibility.
     *
     * @param context The platform-specific context.
     */
    fun initialize(context: KMAuthPlatformContext) {
        this.kmAuthPlatformContext = context
    }

    /**
     * Gets the current authentication configuration.
     *
     * @return The current [KMAuthConfig] or null if not initialized.
     */
    fun getConfig(): KMAuthConfig? = config

    /**
     * Gets the web client ID from the configuration.
     *
     * @return The web client ID or null if not configured.
     */
    fun getWebClientId(): String? = config?.webClientId

    /**
     * Gets the client secret from the configuration.
     *
     * @return The client secret or null if not configured.
     */
    fun getClientSecret(): String? = config?.clientSecret

    /**
     * Gets the Supabase URL from the configuration.
     *
     * @return The Supabase URL or null if not configured.
     */
    fun getSupabaseUrl(): String? = config?.supabaseUrl

    /**
     * Gets the Supabase key from the configuration.
     *
     * @return The Supabase key or null if not configured.
     */
    fun getSupabaseKey(): String? = config?.supabaseKey

    /**
     * Gets the platform context.
     *
     * @return The platform context or null if not initialized.
     */
    fun getKMAuthPlatformContext(): KMAuthPlatformContext? = kmAuthPlatformContext

    // Deprecated methods for backward compatibility
    @Deprecated("Use initialize(KMAuthConfig) instead", ReplaceWith("initialize(KMAuthConfig.forGoogle(context, webClientId, clientSecret))"))
    fun init(webClientId: String, clientSecret: String? = null) {
        this.config = KMAuthConfig.forGoogle(
            context = null,
            webClientId = webClientId,
            clientSecret = clientSecret
        )
    }

    @Deprecated("Use initialize(KMAuthConfig) instead", ReplaceWith("initialize(KMAuthConfig.forGoogle(kmAuthPlatformContext, webClientId))"))
    fun initWithContext(webClientId: String, kmAuthPlatformContext: KMAuthPlatformContext) {
        this.kmAuthPlatformContext = kmAuthPlatformContext
        this.config = KMAuthConfig.forGoogle(
            context = kmAuthPlatformContext,
            webClientId = webClientId
        )
    }

    @Deprecated("Use initialize(KMAuthConfig) instead", ReplaceWith("initialize(KMAuthConfig.forGoogle(kmAuthPlatformContext, webClientId))"))
    fun initContext(kmAuthPlatformContext: KMAuthPlatformContext) {
        this.kmAuthPlatformContext = kmAuthPlatformContext
    }

    @Deprecated("Use initialize(KMAuthConfig) with a config containing the client secret")
    fun initClientSecret(clientSecret: String) {
        this.config = this.config?.copy(clientSecret = clientSecret)
    }
}