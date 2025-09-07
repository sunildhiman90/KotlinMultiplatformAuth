package com.sunildhiman90.kmauth.core

/**
 * Configuration class for KMAuth that supports multiple authentication providers.
 *
 * @property kmAuthPlatformContext The platform-specific context (e.g., Android Context).
 * @property supabaseUrl The Supabase project URL (required for Supabase auth).
 * @property supabaseKey The Supabase anon/public key (required for Supabase auth).
 * @property webClientId The OAuth web client ID (required for Google auth).
 * @property clientSecret The OAuth client secret (required for some OAuth flows).
 */
data class KMAuthConfig(
    /**
     * A unique identifier for the provider (e.g., "google", "supabase").
     * This is used to store and retrieve the configuration for different providers.
     */
    val providerId: String,

    val kmAuthPlatformContext: KMAuthPlatformContext? = null,
    val webClientId: String? = null,
    val clientSecret: String? = null,

    // Supabase
    val supabaseUrl: String? = null,
    val supabaseKey: String? = null,
    val autoLoadFromStorage: Boolean = true,
    val autoRefreshToken: Boolean = true,
    val deepLinkHost: String? = null,
    val deepLinkScheme: String? = null
) {
    init {
        // Validate Supabase configuration if URL or key is provided
        if (supabaseUrl != null || supabaseKey != null) {
            require(!supabaseUrl.isNullOrBlank()) { "supabaseUrl must be provided when using Supabase auth" }
            require(!supabaseKey.isNullOrBlank()) { "supabaseKey must be provided when using Supabase auth" }
        }
    }

    companion object {
        /**
         * Creates a [KMAuthConfig] for Supabase authentication.
         *
         * @param providerId A unique identifier for this provider
         * @param kmAuthPlatformContext The platform-specific context (e.g., Android Context).
         * @param supabaseUrl The Supabase project URL.
         * @param supabaseKey The Supabase anon/public key.
         * @param autoLoadFromStorage Whether to automatically load authentication data from storage.
         * @param autoRefreshToken Whether to automatically refresh access tokens.
         * @param deepLinkHost The host for Android/ios deep links for oauth.
         * @param deepLinkScheme The scheme for Android/ios deep links for oauth.
         */
        fun forSupabase(
            providerId: String = "supabase",
            supabaseUrl: String,
            supabaseKey: String,
            kmAuthPlatformContext: KMAuthPlatformContext? = null,
            autoLoadFromStorage: Boolean = true,
            autoRefreshToken: Boolean = true,
            deepLinkHost: String? = null,
            deepLinkScheme: String? = null
        ): KMAuthConfig = KMAuthConfig(
            providerId = providerId,
            kmAuthPlatformContext = kmAuthPlatformContext,
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey,
            autoLoadFromStorage = autoLoadFromStorage,
            autoRefreshToken = autoRefreshToken,
            deepLinkHost = deepLinkHost,
            deepLinkScheme = deepLinkScheme
        )

        /**
         * Creates a [KMAuthConfig] for Google authentication.
         *
         * @param providerId A unique identifier for this provider (e.g., "google-main", "google-backup").
         * @param kmAuthPlatformContext The platform-specific context (e.g., Android Context).
         * @param webClientId The OAuth web client ID.
         * @param clientSecret The OAuth client secret.
         */
        fun forGoogle(
            webClientId: String,
            providerId: String = "google",
            kmAuthPlatformContext: KMAuthPlatformContext? = null,
            clientSecret: String? = null
        ): KMAuthConfig {
            val kmAuthConfig = KMAuthConfig(
                providerId = providerId,
                webClientId = webClientId,
                clientSecret = clientSecret
            )
            return if (kmAuthPlatformContext != null) {
                kmAuthConfig.copy(
                    kmAuthPlatformContext = kmAuthPlatformContext
                )
            } else {
                kmAuthConfig
            }
        }
    }
}
