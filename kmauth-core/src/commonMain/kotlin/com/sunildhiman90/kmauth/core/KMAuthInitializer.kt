package com.sunildhiman90.kmauth.core

/**
 * This class is used for initialization of KotlinMultiplatformAuth.
 */
object KMAuthInitializer {

    private var configs: MutableMap<String, KMAuthConfig> = mutableMapOf()
    private var kmAuthPlatformContext: KMAuthPlatformContext? = null
    private val providerId = "default"

    /**
     * Initializes the authentication system with the provided configuration for a specific provider.
     *
     * @param config The authentication configuration for the provider.
     */
    fun initialize(config: KMAuthConfig) {
        //if needed in future, we can add providerId as parameter in method
        // Using class-level providerId
        // If config with this providerId exists, merge the new config with existing one
        val existingConfig = configs[providerId]
        val mergedConfig = existingConfig?.copy(
            webClientId = config.webClientId ?: existingConfig.webClientId,
            clientSecret = config.clientSecret ?: existingConfig.clientSecret,
            supabaseUrl = config.supabaseUrl ?: existingConfig.supabaseUrl,
            supabaseKey = config.supabaseKey ?: existingConfig.supabaseKey,
            kmAuthPlatformContext = config.kmAuthPlatformContext
                ?: existingConfig.kmAuthPlatformContext
        )
            ?: config

        configs[providerId] = mergedConfig
        if (config.kmAuthPlatformContext != null) {
            this.kmAuthPlatformContext = config.kmAuthPlatformContext
        }
    }

    /**
     * Gets the configuration for a specific provider.
     *
     * @return The [KMAuthConfig] for the specified provider, or null if not found.
     */
    fun getConfig(): KMAuthConfig? = configs[providerId]

    /**
     * Gets all configurations.
     *
     * @return Map of all provider configurations.
     */
    fun getAllConfigs(): Map<String, KMAuthConfig> = configs.toMap()

    /**
     * Gets the web client ID from the configuration of the specified provider.
     *
     * @return The web client ID or null if not configured.
     */
    fun getWebClientId(providerId: String): String? = configs[providerId]?.webClientId

    /**
     * Gets the client secret from the configuration of the specified provider.
     *
     * @return The client secret or null if not configured.
     */
    fun getClientSecret(providerId: String): String? = configs[providerId]?.clientSecret

    /**
     * Gets the Supabase URL from the configuration of the specified provider.
     *
     * @return The Supabase URL or null if not configured.
     */
    fun getSupabaseUrl(): String? = configs[providerId]?.supabaseUrl

    /**
     * Gets the Supabase key from the configuration of the specified provider.
     *
     * @return The Supabase key or null if not configured.
     */
    fun getSupabaseKey(): String? = configs[providerId]?.supabaseKey

    /**
     * Gets the platform context.
     *
     * @return The platform context or null if not initialized.
     */
    fun getKMAuthPlatformContext(): KMAuthPlatformContext? = kmAuthPlatformContext

    // Deprecated methods for backward compatibility
    @Deprecated(
        "Use initialize(config) with a proper KMAuthConfig instance instead",
        ReplaceWith("initialize(config)")
    )
    fun init(webClientId: String, clientSecret: String? = null) {
        initialize(
            KMAuthConfig(
                providerId = providerId,
                webClientId = webClientId,
                clientSecret = clientSecret
            )
        )
    }

    @Deprecated(
        "Use initialize(config) with a proper KMAuthConfig instance instead",
        ReplaceWith("initialize(config)")
    )
    fun initWithContext(webClientId: String, kmAuthPlatformContext: KMAuthPlatformContext) {
        this.kmAuthPlatformContext = kmAuthPlatformContext
        initialize(
            KMAuthConfig(
                providerId = providerId,
                kmAuthPlatformContext = kmAuthPlatformContext,
                webClientId = webClientId
            )
        )
    }

    fun initContext(kmAuthPlatformContext: KMAuthPlatformContext) {
        this.kmAuthPlatformContext = kmAuthPlatformContext
    }

    /**
     * This method will be used for jvm platform for initialization of clientSecret.
     * Make sure you have initialized the webClientId using KMAuthInitializer.init() or KMAuthInitializer.initialize(config)  with a proper KMAuthConfig instance.
     */
    @Deprecated(
        "Use initialize(config) with a proper KMAuthConfig instance instead",
        ReplaceWith("initialize(config)")
    )
    fun initClientSecret(clientSecret: String) {
        // If null, create a new default config for backward compatibility
        val current = configs[providerId] ?: KMAuthConfig(
            providerId = providerId,
            clientSecret = clientSecret
        )

        // Create or update the config with the client secret
        val newConfig = current.copy(
            clientSecret = clientSecret,
            // Preserve existing values if they exist
            webClientId = current.webClientId,
            supabaseUrl = current.supabaseUrl,
            supabaseKey = current.supabaseKey,
            kmAuthPlatformContext = current.kmAuthPlatformContext
        )
        configs[providerId] = newConfig
    }
}