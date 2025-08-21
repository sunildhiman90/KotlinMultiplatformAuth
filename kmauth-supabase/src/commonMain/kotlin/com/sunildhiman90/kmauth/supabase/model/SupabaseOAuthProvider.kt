package com.sunildhiman90.kmauth.supabase.model

import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Bitbucket
import io.github.jan.supabase.auth.providers.Discord
import io.github.jan.supabase.auth.providers.Facebook
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.Gitlab
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.LinkedIn
import io.github.jan.supabase.auth.providers.OAuthProvider
import io.github.jan.supabase.auth.providers.Slack
import io.github.jan.supabase.auth.providers.Spotify
import io.github.jan.supabase.auth.providers.Twitter

/**
 * Represents the available OAuth providers for Supabase authentication.
 */
enum class SupabaseOAuthProvider {
    GOOGLE,
    GITHUB,
    GITLAB,
    BITBUCKET,
    FACEBOOK,
    TWITTER,
    DISCORD,
    SLACK,
    SPOTIFY,
    TWITCH,
    LINKEDIN,
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
        }
    }
}
