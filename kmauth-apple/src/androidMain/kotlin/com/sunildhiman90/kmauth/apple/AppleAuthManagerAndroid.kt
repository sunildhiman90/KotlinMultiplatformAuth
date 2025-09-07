package com.sunildhiman90.kmauth.apple

/**
 * Android implementation of AppleAuthManager that delegates to the common AppleSupabaseAuthManager.
 */
class AppleAuthManagerAndroid : AppleAuthManager by AppleSupabaseAuthManager()
