package com.sunildhiman90.kmauth.core

/**
 * This class is used for initialization of KotlinMultiplatformAuth.
 */
object KMAuthInitializer {

    private var webClientId: String? = null
    private var clientSecret: String? = null
    private var kmAuthPlatformContext: KMAuthPlatformContext? = null

    /**
     * This method can be used for all platforms for initialization of webClientId.
     * clientSecret is optional for other platforms except jvm, it will be used only for jvm.
     * If we don't want to call this method from each platform, we can call initClientSecret(clientSecret: String) method
     * from jvm source set and KMAuthInitializer.init(webClientId: String) from composable for all platforms.
     */
    fun init(webClientId: String, clientSecret: String? = null) {
        this.webClientId = webClientId
        clientSecret?.let {
            this.clientSecret = clientSecret
        }
    }

    /**
     * This method will be used for jvm platform for initialization of clientSecret.
     * If we don't want to call [KMAuthInitializer.init(webClientId: String, clientSecret: String?)] from each platform,
     * we can call this method from jvm source set and KMAuthInitializer.init(webClientId: String) from composable for all platforms.
     */
    fun initClientSecret(clientSecret: String) {
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