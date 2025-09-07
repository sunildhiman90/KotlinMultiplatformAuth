package com.sunildhiman90.kmauth.supabase.model

import io.github.jan.supabase.auth.providers.builtin.DefaultAuthProvider
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.builtin.Phone
import io.github.jan.supabase.auth.user.UserInfo

/**
 * Represents the available default auth providers for Supabase authentication for KotlinMultiplatformAuth
 * Its a wrapper for [io.github.jan.supabase.auth.providers.builtin.DefaultAuthProvider]
 *
 * @property EMAIL
 * @property ID_TOKEN
 * @property PHONE
 */
enum class SupabaseDefaultAuthProvider {
    EMAIL,
    ID_TOKEN,
    PHONE;

    internal fun toDefaultAuthProvider(): DefaultAuthProvider<out DefaultAuthProvider.Config, UserInfo> {
        return when (this) {
            EMAIL -> Email
            ID_TOKEN -> IDToken
            PHONE -> Phone
        }
    }

}