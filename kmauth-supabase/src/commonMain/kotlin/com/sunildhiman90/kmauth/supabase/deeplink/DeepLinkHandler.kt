package com.sunildhiman90.kmauth.supabase.deeplink

/**
 * DeepLinkHandler is an interface for handling deep links in Supabase authentication for android and ios.
 */
expect class DeepLinkHandler

expect fun getDeepLinkHandler(): DeepLinkHandler
