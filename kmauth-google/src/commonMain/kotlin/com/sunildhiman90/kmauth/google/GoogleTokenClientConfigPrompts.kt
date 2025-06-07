package com.sunildhiman90.kmauth.google

/**
 * Optional, defaults to 'select_account'. A space-delimited, case-sensitive list of prompts to present the user. Possible values are:
 * empty string The user will be prompted only the first time your app requests access. Cannot be specified with other values.
 * 'none' Do not display any authentication or consent screens. Must not be specified with other values.
 * 'consent' Prompt the user for consent.
 * 'select_account' Prompt the user to select an account.
 */
internal enum class GoogleTokenClientConfigPrompt(val value: String) {
    NONE("none"),
    CONSENT("consent"),
    SELECT_ACCOUNT("select_account"),
    EMPTY("");
}