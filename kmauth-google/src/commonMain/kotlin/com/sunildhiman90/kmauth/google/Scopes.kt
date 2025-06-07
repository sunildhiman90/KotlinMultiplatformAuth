package com.sunildhiman90.kmauth.google

object Scopes {
    val defaultScopes = listOf(
        Scopes.OPENID,
        Scopes.EMAIL,
        Scopes.PROFILE
    )

    const val OPENID = "openid"
    const val EMAIL = "email"
    const val PROFILE = "profile"
}