package com.sunildhiman90.kmauth.google.externals

//For js global object variables, we can use external object
external object google {
    object accounts {
        object id {
            fun initialize(config: JsAny)
            fun renderButton(element: JsAny, options: JsAny)
            fun prompt(callback: (response: PromptMomentNotification) -> Unit)
            fun disableAutoSelect()

            object PromptMomentNotification {
                fun isDisplayMoment(): Boolean?
                fun isDisplayed(): Boolean?
                fun isNotDisplayed(): Boolean?
                fun getNotDisplayedReason(): String?
                fun isSkippedMoment(): Boolean?
                fun getSkippedReason(): Boolean?
                fun isDismissedMoment(): Boolean?
                fun getDismissedReason(): String?
                fun getMomentType(): String?
            }
        }
    }
}

