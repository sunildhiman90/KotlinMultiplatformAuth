package com.sunildhiman90.kmauth.supabase.deeplink

import com.sunildhiman90.kmauth.supabase.KMAuthSupabase
import io.github.jan.supabase.auth.handleDeeplinks
import platform.Foundation.NSURL

actual class DeepLinkHandler {

    fun handleDeepLinks(url: NSURL) {
        KMAuthSupabase.getSupabaseClient().handleDeeplinks(url)
    }
}

actual fun getDeepLinkHandler(): DeepLinkHandler {
    return DeepLinkHandler()
}