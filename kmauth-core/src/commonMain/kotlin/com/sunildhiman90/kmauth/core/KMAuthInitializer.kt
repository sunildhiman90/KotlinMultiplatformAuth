package com.sunildhiman90.kmauth.core

object KMAuthInitializer {

    private var webClientId: String? = null
    private var clientSecret: String? = null
    private var kmAuthPlatformContext: KMAuthPlatformContext? = null

    /**
     * This method can be used for all platforms for initialization of webClientId.
     * clientSecret is optional, it will be used only for jvm
     */
    fun init(webClientId: String, clientSecret: String? = null) {
        this.webClientId = webClientId
        this.clientSecret = clientSecret
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

    fun getClientSecret(): String? {
        return clientSecret
    }

    fun getKMAuthPlatformContext(): KMAuthPlatformContext? {
        return kmAuthPlatformContext
    }

}