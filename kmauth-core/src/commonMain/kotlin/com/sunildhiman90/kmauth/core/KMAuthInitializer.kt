package com.sunildhiman90.kmauth.core

object KMAuthInitializer {

    private var webClientId: String? = null
    private var kmAuthPlatformContext: KMAuthPlatformContext? = null

    /**
     * This method can be used for all platforms for initialization of webClientId.
     */
    fun init(webClientId: String) {
        this.webClientId = webClientId
    }

    /**
     * This method can be used for webClientId and Android platform for initialization of KMAuthPlatformContext.
     */
    fun initWithContext(webClientId: String, kmAuthPlatformContext: KMAuthPlatformContext) {
        this.webClientId = webClientId
        this.kmAuthPlatformContext = kmAuthPlatformContext
    }

    /**
     * This method can be used for Android platform for initialization of KMAuthPlatformContext.
     */
    fun initContext(kmAuthPlatformContext: KMAuthPlatformContext) {
        this.kmAuthPlatformContext = kmAuthPlatformContext
    }

    fun getWebClientId(): String? {
        return webClientId
    }

    fun getKMAuthPlatformContext(): KMAuthPlatformContext? {
        return kmAuthPlatformContext
    }

}