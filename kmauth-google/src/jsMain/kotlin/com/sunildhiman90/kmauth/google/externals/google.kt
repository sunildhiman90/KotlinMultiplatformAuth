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

external interface CredentialResponse {
    val credential: String?
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
    }
}
