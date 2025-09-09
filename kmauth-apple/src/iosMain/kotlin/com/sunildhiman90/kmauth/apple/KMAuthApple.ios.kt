package com.sunildhiman90.kmauth.apple

import com.sunildhiman90.kmauth.supabase.KMAuthSupabase
import com.sunildhiman90.kmauth.supabase.deeplink.DeepLinkHandler

actual object KMAuthApple {
    actual val appleAuthManager: AppleAuthManager
        get() = AppleAuthManagerIOS()

    actual fun deepLinkHandler(): DeepLinkHandler = KMAuthSupabase.deepLinkHandler()
}