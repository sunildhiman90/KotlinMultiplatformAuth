package com.sunildhiman90.kmauth.apple

/**
 * WebAssembly (WasmJS) implementation of AppleAuthManager that delegates to the common AppleSupabaseAuthManager.
 */
class AppleAuthManagerWasmJs : AppleAuthManager by AppleSupabaseAuthManager()