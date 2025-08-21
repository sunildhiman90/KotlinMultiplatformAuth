package com.sunildhiman90.kmauth.supabase.model

import io.github.jan.supabase.auth.providers.ExternalAuthConfig

class SupabaseExternalAuthConfig(): SupabaseExternalAuthConfigDefaults() {

    fun toExternalAuthConfig(): ExternalAuthConfig {
        return ExternalAuthConfig().apply {
            this.scopes.addAll(this@SupabaseExternalAuthConfig.scopes)
            this.queryParams.putAll(this@SupabaseExternalAuthConfig.queryParams)
            this.automaticallyOpenUrl = this@SupabaseExternalAuthConfig.automaticallyOpenUrl
        }
    }
}

open class SupabaseExternalAuthConfigDefaults {

    /**
     * The scopes to request from the external provider
     */
    val scopes = mutableListOf<String>()

    /**
     * Additional query parameters to send to the external provider
     */
    val queryParams = mutableMapOf<String, String>()

    /**
     * Automatically open the URL in the browser. Only applies to [Auth.linkIdentity].
     */
    var automaticallyOpenUrl: Boolean = true

}