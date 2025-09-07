package com.sunildhiman90.kmauth.supabase.model

import com.sunildhiman90.kmauth.core.KMAuthUser
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.ExperimentalTime

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
     * The user's access token
     */
    val accessToken: String? = null,

    /**
     * The user's creation timestamp
     */
    val createdAt: String? = null,

    /**
     * The user's last update timestamp
     */
    val updatedAt: String? = null
)


/**
 * Converts a Supabase UserInfo object to a SupabaseUser.
 */
@OptIn(ExperimentalTime::class)
fun io.github.jan.supabase.auth.user.UserInfo.toSupabaseUser(): SupabaseUser {
    return SupabaseUser(
        id = this.id,
        email = this.email,
        phone = this.phone,
        name = this.userMetadata?.get("full_name")?.jsonPrimitive?.content,
        firstName = this.userMetadata?.get("first_name")?.jsonPrimitive?.content,
        lastName = this.userMetadata?.get("last_name")?.jsonPrimitive?.content,
        avatarUrl = this.userMetadata?.get("avatar_url")?.jsonPrimitive?.content,
        provider = this.appMetadata?.get("provider")?.jsonPrimitive?.content,
        emailConfirmedAt = this.emailConfirmedAt?.toString(),
        phoneConfirmedAt = this.phoneConfirmedAt?.toString(),
        lastSignInAt = this.lastSignInAt?.toString(),
        createdAt = this.createdAt?.toString(),
        updatedAt = this.updatedAt?.toString()
    )
}

fun SupabaseUser.toKMAuthUser(): KMAuthUser =
    KMAuthUser(
        id = id,
        email = email ?: "",
        accessToken = accessToken,
        name = name,
        phoneNumber = phone,
        profilePicUrl = avatarUrl
    )