package com.sunildhiman90.kmauth.supabase.deeplink

import android.content.Intent
import com.sunildhiman90.kmauth.supabase.KMAuthSupabase
import io.github.jan.supabase.auth.handleDeeplinks


actual class DeepLinkHandler {

    fun handleDeepLinks(intent: Intent) {
        KMAuthSupabase.getSupabaseClient().handleDeeplinks(intent)
    }
}


actual fun getDeepLinkHandler(): DeepLinkHandler = DeepLinkHandler()