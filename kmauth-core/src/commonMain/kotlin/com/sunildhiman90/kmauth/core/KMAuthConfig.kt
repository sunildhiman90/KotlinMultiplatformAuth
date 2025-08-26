package com.sunildhiman90.kmauth.core

/**
 * Configuration class for KMAuth that supports multiple authentication providers.
 *
 * @property context The platform-specific context (e.g., Android Context).
 * @property supabaseUrl The Supabase project URL (required for Supabase auth).
 * @property supabaseKey The Supabase anon/public key (required for Supabase auth).
 * @property webClientId The OAuth web client ID (required for Google auth).
 * @property clientSecret The OAuth client secret (required for some OAuth flows).
 */
data class KMAuthConfig(
    val context: Any? = null,
    val webClientId: String? = null,
    val clientSecret: String? = null,

    //supabase
    val supabaseUrl: String? = null,
    val supabaseKey: String? = null,
    val autoLoadFromStorage: Boolean = true,
    val autoRefreshToken: Boolean = true,
    val androidDeepLinkHost: String? = null,
    val androidDeepLinkScheme: String? = null
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
         * @param context The platform-specific context (e.g., Android Context).
         * @param supabaseUrl The Supabase project URL.
         * @param supabaseKey The Supabase anon/public key.
         * @param autoLoadFromStorage Whether to automatically load authentication data from storage.
         * @param autoRefreshToken Whether to automatically refresh access tokens.
         * @param androidDeepLinkHost The host for Android deep links for oauth.
         * @param androidDeepLinkScheme The scheme for Android deep links for oauth.
         */
        fun forSupabase(
            context: Any? = null,
            supabaseUrl: String,
            supabaseKey: String,
            autoLoadFromStorage: Boolean = true,
            autoRefreshToken: Boolean = true,
            androidDeepLinkHost: String? = null,
            androidDeepLinkScheme: String? = null
        ): KMAuthConfig {
            return KMAuthConfig(
                context = context,
                supabaseUrl = supabaseUrl,
                supabaseKey = supabaseKey,
                webClientId = null,
                clientSecret = null,
                autoLoadFromStorage = autoLoadFromStorage,
                autoRefreshToken = autoRefreshToken,
                androidDeepLinkHost = androidDeepLinkHost,
                androidDeepLinkScheme = androidDeepLinkScheme
            )
        }

        /**
         * Creates a [KMAuthConfig] for Google OAuth authentication.
         *
         * @param context The platform-specific context (e.g., Android Context).
         * @param webClientId The OAuth web client ID.
         * @param clientSecret The OAuth client secret (required for server-side flows).
         */
        fun forGoogle(
            context: Any? = null,
            webClientId: String? = null,
            clientSecret: String? = null,
        ): KMAuthConfig {
            return KMAuthConfig(
                context = context,
                webClientId = webClientId,
                clientSecret = clientSecret
            )
        }
    }
}
