package com.sunildhiman90.kmauth.apple

import com.sunildhiman90.kmauth.supabase.deeplink.DeepLinkHandler

expect object KMAuthApple {
    val appleAuthManager: AppleAuthManager

    fun deepLinkHandler(): DeepLinkHandler
}