package com.sunildhiman90.kmauth.google

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleUser(
    @SerialName("id")
    val id: String,
    @SerialName("email")
    val email: String,
    @SerialName("verified_email")
    val verifiedEmail: Boolean,
    @SerialName("name")
    val name: String,
    @SerialName("given_name")
    val givenName: String,
    @SerialName("family_name")
    val familyName: String,
    @SerialName("picture")
    val picture: String
)