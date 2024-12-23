package com.sunildhiman90.kmauth.core

data class KMAuthUser(
    val id: String,
    val idToken: String,
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val profilePicUrl: String? = null,
)