package com.sunildhiman90.kmauth.supabase.model

import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Azure
import io.github.jan.supabase.auth.providers.Bitbucket
import io.github.jan.supabase.auth.providers.Discord
import io.github.jan.supabase.auth.providers.Facebook
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.Gitlab
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.IDTokenProvider
import io.github.jan.supabase.auth.providers.Keycloak
import io.github.jan.supabase.auth.providers.LinkedIn
import io.github.jan.supabase.auth.providers.OAuthProvider
import io.github.jan.supabase.auth.providers.Slack
import io.github.jan.supabase.auth.providers.Spotify
import io.github.jan.supabase.auth.providers.Twitter

/**
 * Represents the available OAuth providers for Supabase authentication for KotlinMultiplatformAuth
 * Its a wrapper for [io.github.jan.supabase.auth.providers.OAuthProvider]
 */
enum class SupabaseOAuthProvider {
    GITHUB,
    GITLAB,
    BITBUCKET,
    TWITTER,
    DISCORD,
    SLACK,
    SPOTIFY,
    TWITCH,
    LINKEDIN,
    KEYCLOAK,

    // IDTokenProviders
    GOOGLE,
    FACEBOOK,
    AZURE,
    APPLE;

    internal fun toOAuthProvider(): OAuthProvider {
        return when (this) {
            GOOGLE -> Google
            GITHUB -> Github
            GITLAB -> Gitlab
            BITBUCKET -> Bitbucket
            FACEBOOK -> Facebook
            TWITTER -> Twitter
            DISCORD -> Discord
            SLACK -> Slack
            SPOTIFY -> Spotify
            TWITCH -> Twitter
            LINKEDIN -> LinkedIn
            APPLE -> Apple
            AZURE -> Azure
            KEYCLOAK -> Keycloak
        }
    }

    internal fun toIdTokenProvider(): IDTokenProvider {
        return when (this) {
            GOOGLE -> Google
            APPLE -> Apple
            FACEBOOK -> Facebook
            AZURE -> Azure
            else -> throw IllegalArgumentException("Provider $this is not supported for ID token authentication")
        }
    }
}
