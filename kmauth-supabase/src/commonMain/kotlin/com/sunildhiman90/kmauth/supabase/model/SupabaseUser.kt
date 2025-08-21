package com.sunildhiman90.kmauth.supabase.model

import kotlinx.serialization.Serializable

/**
 * Data class representing a Supabase user
 */
@Serializable
data class SupabaseUser(
    /**
     * The unique identifier for the user
     */
    val id: String,

    /**
     * The user's email address
     */
    val email: String? = null,

    /**
     * The user's phone number
     */
    val phone: String? = null,

    /**
     * The user's full name
     */
    val name: String? = null,

    /**
     * The user's first name
     */
    val firstName: String? = null,

    /**
     * The user's last name
     */
    val lastName: String? = null,

    /**
     * URL to the user's profile picture
     */
    val avatarUrl: String? = null,

    /**
     * The user's authentication provider (e.g., "google", "apple")
     */
    val provider: String? = null,

    /**
     * The user's email verification status
     */
    val emailConfirmedAt: String? = null,

    /**
     * The user's phone verification status
     */
    val phoneConfirmedAt: String? = null,

    /**
     * The user's last sign-in timestamp
     */
    val lastSignInAt: String? = null,

    /**
     * The user's creation timestamp
     */
    val createdAt: String? = null,

    /**
     * The user's last update timestamp
     */
    val updatedAt: String? = null
)
