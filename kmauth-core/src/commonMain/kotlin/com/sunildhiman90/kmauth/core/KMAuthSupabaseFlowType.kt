package com.sunildhiman90.kmauth.core


/**
 * Enum class for Supabase authentication flow types.
 */
enum class KMAuthSupabaseFlowType {
    IMPLICIT,
    PKCE;

    companion object {
        fun fromValue(value: String): KMAuthSupabaseFlowType {
            return entries.firstOrNull { it.name == value } ?: throw IllegalArgumentException("Invalid KMAuthSupabaseFlowType: $value")
        }
    }
}