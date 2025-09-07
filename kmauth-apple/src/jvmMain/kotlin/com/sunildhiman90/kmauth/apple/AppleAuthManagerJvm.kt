package com.sunildhiman90.kmauth.apple

/**
 * JVM implementation of AppleAuthManager that delegates to the common AppleSupabaseAuthManager.
 */
class AppleAuthManagerJvm : AppleAuthManager by AppleSupabaseAuthManager()
