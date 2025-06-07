package com.sunildhiman90.kmauth.google.externals

import org.w3c.dom.HTMLElement

// https://developers.google.com/identity/gsi/web/reference/js-reference#IdConfiguration
external interface GoogleIdConfiguration {
    var client_id: String?
    var ux_mode: String?
    var use_fedcm_for_prompt: Boolean?
    var callback: (response: String?) -> Unit
}

external interface GsiButtonConfiguration {
    var type: String?
    var theme: String?
    var size: String?
    var text: String?
    var shape: String?
    var logo_alignment: String?
}

//https://developers.google.com/identity/gsi/web/reference/js-reference#CredentialResponse
external interface CredentialResponse {
    val credential: String?
}


//https://developers.google.com/identity/oauth2/web/reference/js-reference#TokenResponse
external interface TokenResponse {
    var access_token: String?
    var id_token: String?
    var expires_in: Number?
    var scope: String?
    var token_type: String?
    var error: String?
}

//https://developers.google.com/identity/oauth2/web/reference/js-reference#TokenClientConfig
external interface TokenClientConfig {
    var client_id: String?
    var scope: String?
    var include_granted_scopes: Boolean?
    var callback: (response: String?) -> Unit
    var prompt: String?
    var error_description: String?
}

external interface OverridableTokenClientConfig {
    var scope: String?
    var include_granted_scopes: Boolean?
    var prompt: String?
}


external interface TokenClient {
    fun requestAccessToken(overrideConfig: OverridableTokenClientConfig)
}

external object google {
    object accounts {
        object id {
            fun initialize(config: GoogleIdConfiguration) // Use the external interface
            //https://developers.google.com/identity/gsi/web/reference/js-reference#google.accounts.id.renderButton
            fun renderButton(
                element: HTMLElement,
                options: GsiButtonConfiguration
            ) // Use the external interface

            fun prompt(callback: (response: PromptMomentNotification) -> Unit)
            fun disableAutoSelect()

            // This is also likely an external JS object structure
            object PromptMomentNotification {
                fun isDisplayMoment(): Boolean?
                fun isDisplayed(): Boolean?
                fun isNotDisplayed(): Boolean?
                fun getNotDisplayedReason(): String?
                fun isSkippedMoment(): Boolean?
                fun getSkippedReason(): String?
                fun isDismissedMoment(): Boolean?
                fun getDismissedReason(): String?
                fun getMomentType(): String?
            }
        }

        object oauth2 {
            fun initTokenClient(config: TokenClientConfig): TokenClient // Use the external interface
        }
    }
}


external interface GoogleUserJs {
    val id: String?
    val email: String?
    val name: String?
    val given_name: String?
    val family_name: String?
    val verified_email: Boolean?
    val picture: String?
}