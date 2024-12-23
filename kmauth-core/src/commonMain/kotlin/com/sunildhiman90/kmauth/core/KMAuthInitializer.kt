package com.sunildhiman90.kmauth.core

object KMAuthInitializer {

    private var webClientId: String? = null
    private var kmAuthPlatformContext: KMAuthPlatformContext? = null

    fun init(webClientId: String, kmAuthPlatformContext: KMAuthPlatformContext) {
        this.webClientId = webClientId
        this.kmAuthPlatformContext = kmAuthPlatformContext
    }

    fun getWebClientId(): String? {
        return webClientId
    }

    fun getKMAuthPlatformContext(): KMAuthPlatformContext? {
        return kmAuthPlatformContext
    }

}