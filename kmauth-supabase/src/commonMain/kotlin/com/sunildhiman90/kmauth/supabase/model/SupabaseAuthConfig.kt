package com.sunildhiman90.kmauth.supabase.model

import co.touchlab.kermit.Logger
import com.sunildhiman90.kmauth.supabase.model.PhoneConfirmationChannel.Companion.toSupabaseChannel
import io.github.jan.supabase.auth.providers.ExternalAuthConfig
import io.github.jan.supabase.auth.providers.builtin.DefaultAuthProvider
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.builtin.Phone

class SupabaseAuthConfig(): SupabaseAuthConfigDefaults()

fun SupabaseAuthConfig.toExternalAuthConfig(): ExternalAuthConfig {
    val supabaseAuthConfig = this
    return ExternalAuthConfig().apply {
        this.scopes.addAll(supabaseAuthConfig.scopes)
        this.queryParams.putAll(supabaseAuthConfig.queryParams)
        this.automaticallyOpenUrl = supabaseAuthConfig.automaticallyOpenUrl
    }
}

fun SupabaseAuthConfig.toDefaultAuthProviderConfig(provider: SupabaseDefaultAuthProvider): DefaultAuthProvider.Config {
    Logger.withTag("SupabaseAuthConfig").i("Converting SupabaseAuthConfig to DefaultAuthProvider.Config: $this")
    val supabaseAuthConfig = this
    return when(provider) {
        SupabaseDefaultAuthProvider.EMAIL -> Email.Config().apply {
            this.email = supabaseAuthConfig.email
            this.password = supabaseAuthConfig.password
        }
        SupabaseDefaultAuthProvider.ID_TOKEN -> IDToken.Config().apply {
            Logger.withTag("SupabaseAuthConfig").i("idToken: ${supabaseAuthConfig.idToken}")
            Logger.withTag("SupabaseAuthConfig").i("provider: ${supabaseAuthConfig.provider}")
            Logger.withTag("SupabaseAuthConfig").i("accessToken: ${supabaseAuthConfig.accessToken}")
            Logger.withTag("SupabaseAuthConfig").i("nonce: ${supabaseAuthConfig.nonce}")
            Logger.withTag("SupabaseAuthConfig").i("supabaseUrl: ${supabaseAuthConfig.supabaseUrl}")
            this.idToken = supabaseAuthConfig.idToken
            this.provider = supabaseAuthConfig.provider?.toIdTokenProvider()
            this.accessToken = supabaseAuthConfig.accessToken
            this.nonce = supabaseAuthConfig.nonce
        }
        SupabaseDefaultAuthProvider.PHONE -> Phone.Config().apply {
            this.phone = supabaseAuthConfig.phone
            this.password = supabaseAuthConfig.password
            this.channel = supabaseAuthConfig.channel.toSupabaseChannel()
        }
    }
}

open class SupabaseAuthConfigDefaults {

    /**
     * The Supabase project URL (e.g., https://your-project-ref.supabase.co)
     * This is used as the audience for ID token validation
     */
    var supabaseUrl: String? = null

    /**
     * The scopes to request from the external provider
     */
    var scopes = mutableListOf<String>()

    /**
     * Additional query parameters to send to the external provider
     */
    var queryParams = mutableMapOf<String, String>()

    /**
     * Automatically open the URL in the browser. Only applies to [io.github.jan.supabase.auth.Auth.linkIdentity].
     */
    var automaticallyOpenUrl: Boolean = true


    // Default Auth Provider Fields
    var email: String = ""

    var phone: String = ""

    var password: String = ""

    // Phone number confirmation channel
    var channel: PhoneConfirmationChannel = PhoneConfirmationChannel.SMS

    var idToken: String = ""

    var provider: SupabaseOAuthProvider? = null

    var accessToken: String? = null

    var nonce: String? = null


}

enum class PhoneConfirmationChannel(val value: String) {
    /**
     * Send the confirmation via SMS
     */
    SMS("sms"),

    /**
     * Send the confirmation via WhatsApp. **Note:** WhatsApp is only supported by Twilio
     */
    WHATSAPP("whatsapp");

    companion object {
        fun fromValue(value: String): PhoneConfirmationChannel {
            return entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Invalid value: $value")
        }

        fun PhoneConfirmationChannel.toSupabaseChannel(): Phone.Channel {
            return when(this) {
                SMS -> Phone.Channel.SMS
                WHATSAPP -> Phone.Channel.WHATSAPP
            }
        }
    }
}