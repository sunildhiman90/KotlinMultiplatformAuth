package com.sunildhiman90.kmauth.google.jsUtils

import com.sunildhiman90.kmauth.google.externals.CredentialResponse
import com.sunildhiman90.kmauth.google.externals.GoogleIdConfiguration
import com.sunildhiman90.kmauth.google.externals.GsiButtonConfiguration


internal fun googleIdConfig(clientId: String, callback: (CredentialResponse) -> Unit): GoogleIdConfiguration = js(
    """
    ({
        client_id: clientId,
        ux_mode: "popup",
        callback: callback,
        use_fedcm_for_prompt: true
    })
    """
)

internal fun gsiButtonConfig(theme: String, size: String): GsiButtonConfiguration  = js(
    """
    ({
        theme: theme,
        size: size
    })
    """
)