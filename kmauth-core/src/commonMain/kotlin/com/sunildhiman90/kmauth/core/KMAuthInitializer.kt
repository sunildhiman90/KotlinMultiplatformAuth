package com.sunildhiman90.kmauth.core

/**
 * This class is used for initialization of KotlinMultiplatformAuth.
 */
object KMAuthInitializer {

    private var configs: MutableMap<String, KMAuthConfig> = mutableMapOf()
    private var kmAuthPlatformContext: KMAuthPlatformContext? = null

    /**
     * Initializes the authentication system with the provided configuration for a specific provider.
     *
     * @param providerId A unique identifier for the provider (e.g., "google", "apple", "supabase").
     * @param config The authentication configuration for the provider.
     */
    fun initialize(providerId: String = "default", config: KMAuthConfig) {
        configs[providerId] = config
        if (config.kmAuthPlatformContext != null) {
            this.kmAuthPlatformContext = config.kmAuthPlatformContext
        }
    }
    
    /**
     * Gets the configuration for a specific provider.
     *
     * @param providerId The unique identifier for the provider.
     * @return The [KMAuthConfig] for the specified provider, or null if not found.
     */
    fun getConfig(providerId: String): KMAuthConfig? = configs[providerId]
    
    /**
     * Gets all configurations.
     *
     * @return Map of all provider configurations.
     */
    fun getAllConfigs(): Map<String, KMAuthConfig> = configs.toMap()

    /**
     * Gets the web client ID from the configuration of the specified provider.
     *
     * @param providerId The unique identifier for the provider.
     * @return The web client ID or null if not configured.
     */
    fun getWebClientId(providerId: String): String? = configs[providerId]?.webClientId

    /**
     * Gets the client secret from the configuration of the specified provider.
     *
     * @param providerId The unique identifier for the provider.
     * @return The client secret or null if not configured.
     */
    fun getClientSecret(providerId: String): String? = configs[providerId]?.clientSecret

    /**
     * Gets the Supabase URL from the configuration of the specified provider.
     *
     * @param providerId The unique identifier for the provider.
     * @return The Supabase URL or null if not configured.
     */
    fun getSupabaseUrl(providerId: String): String? = configs[providerId]?.supabaseUrl

    /**
     * Gets the Supabase key from the configuration of the specified provider.
     *
     * @param providerId The unique identifier for the provider.
     * @return The Supabase key or null if not configured.
     */
    fun getSupabaseKey(providerId: String): String? = configs[providerId]?.supabaseKey

    /**
     * Gets the platform context.
     *
     * @return The platform context or null if not initialized.
     */
    fun getKMAuthPlatformContext(): KMAuthPlatformContext? = kmAuthPlatformContext

    // Deprecated methods for backward compatibility
    @Deprecated(
        "Use initialize(providerId, config) with a proper KMAuthConfig instance instead",
        ReplaceWith("initialize(providerId, config)")
    )
    fun init(webClientId: String, clientSecret: String? = null) {
        initialize("default", KMAuthConfig(
            providerId = "default",
            webClientId = webClientId,
            clientSecret = clientSecret
        ))
    }

    @Deprecated(
        "Use initialize(providerId, config) with a proper KMAuthConfig instance instead",
        ReplaceWith("initialize(providerId, config)")
    )
    fun initWithContext(webClientId: String, kmAuthPlatformContext: KMAuthPlatformContext) {
        this.kmAuthPlatformContext = kmAuthPlatformContext
        initialize("default", KMAuthConfig(
            providerId = "default",
            kmAuthPlatformContext = kmAuthPlatformContext,
            webClientId = webClientId
        ))
    }

    @Deprecated(
        "Use initialize(providerId, config) with a proper KMAuthConfig instance instead",
        ReplaceWith("initialize(providerId, config)")
    )
    fun initContext(kmAuthPlatformContext: KMAuthPlatformContext) {
        this.kmAuthPlatformContext = kmAuthPlatformContext
    }

    @Deprecated(
        "Use initialize(providerId, config) with a proper KMAuthConfig instance instead",
        ReplaceWith("initialize(providerId, config)")
    )
    /**
     * This method will be used for jvm platform for initialization of clientSecret.
     * Make sure you have initialized the webClientId using KMAuthInitializer.init() or KMAuthInitializer.initialize(providerId, config)  with a proper KMAuthConfig instance.
     */
    fun initClientSecret(clientSecret: String) {
        val current = configs["default"] ?: 
            throw IllegalStateException("No default configuration found. Initialize with a provider first using [KMAuthInitializer.initialize(providerId, config)] with a proper KMAuthConfig instance instead.")
        
        // Create a new config with the updated client secret
        val newConfig = current.copy(
            clientSecret = clientSecret,
        )
        configs["default"] = newConfig
    }
}