package com.sunildhiman90.kmauth.core

data class KMAuthUser(
    var id: String,
    val idToken: String? = null,
    val accessToken: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val profilePicUrl: String? = null,
)