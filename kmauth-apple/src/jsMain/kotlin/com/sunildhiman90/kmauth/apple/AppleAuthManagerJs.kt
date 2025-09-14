package com.sunildhiman90.kmauth.apple

/**
 * JavaScript implementation of AppleAuthManager that delegates to the common AppleSupabaseAuthManager.
 */
class AppleAuthManagerJs : AppleAuthManager by AppleSupabaseAuthManager()