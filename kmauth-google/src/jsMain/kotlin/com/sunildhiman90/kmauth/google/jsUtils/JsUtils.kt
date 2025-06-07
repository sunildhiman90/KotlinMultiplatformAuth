package com.sunildhiman90.kmauth.google.jsUtils

import com.sunildhiman90.kmauth.google.externals.CredentialResponse
import com.sunildhiman90.kmauth.google.externals.GoogleIdConfiguration
import com.sunildhiman90.kmauth.google.externals.GoogleUserJs
import com.sunildhiman90.kmauth.google.externals.GsiButtonConfiguration
import com.sunildhiman90.kmauth.google.externals.OverridableTokenClientConfig
import com.sunildhiman90.kmauth.google.externals.TokenClientConfig
import com.sunildhiman90.kmauth.google.externals.TokenResponse
import org.w3c.fetch.DEFAULT
import org.w3c.fetch.FOLLOW
import org.w3c.fetch.RequestCache
import org.w3c.fetch.RequestCredentials
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode
import org.w3c.fetch.RequestRedirect


internal fun googleIdConfig(
    clientId: String,
    callback: (CredentialResponse) -> Unit
): GoogleIdConfiguration = js(
    """
    ({
        client_id: clientId,
        ux_mode: "popup",
        callback: callback,
        use_fedcm_for_prompt: true
    })
    """
)

internal fun gsiButtonConfig(theme: String, size: String): GsiButtonConfiguration = js(
    """
    ({
        theme: theme,
        size: size
    })
    """
)

internal fun tokenClientConfig(
    clientId: String,
    scope: String,
    callback: (TokenResponse) -> Unit
): TokenClientConfig = js(
    """
    ({
        client_id: clientId,
        scope: scope,
        callback: callback,
    })
    """
)

internal fun overrideTokenClientConfig(
    scope: String,
    includeGrantedScopes: Boolean,
    prompt: String
): OverridableTokenClientConfig = js(
    """
    ({
        scope: scope,
        include_granted_scopes: includeGrantedScopes,
        prompt: prompt,
    })
    """
)

internal fun convertToGoogleUserInfo(userInfo: dynamic): GoogleUserJs = js(
    """
    ({
        id: userInfo.id,
        name: userInfo.name,
        email: userInfo.email,
        picture: userInfo.picture,
        given_name: userInfo.given_name,
        family_name: userInfo.family_name,
        verified_email: userInfo.verified_email,        
    })
    """
)


//internal fun RequestInit(
//    method: String? = null,
//    headers: dynamic? /* Headers|JsArray<JsArray<JsString>>|OpenEndedDictionary<JsString> */ = null,
//    body: dynamic? /* Blob|BufferSource|FormData|URLSearchParams|String */ = null,
//    referrer: String? = null,
//    referrerPolicy: dynamic? = null,
//    mode: RequestMode? = null,
//    credentials: RequestCredentials? = null,
//    cache: RequestCache? = RequestCache.DEFAULT,
//    redirect: RequestRedirect? = RequestRedirect.FOLLOW,
//    integrity: String? = null,
//    keepalive: Boolean? = null,
//    window: dynamic? = null
//): RequestInit = js(
//        """
//        ({
//            method: method,
//            headers: headers,
//            body: body,
//            referrer: referrer,
//            referrerPolicy: referrerPolicy,
//            mode: mode,
//            credentials: credentials,
//            cache: cache,
//            redirect: redirect,
//            integrity: integrity,
//            keepalive: keepalive,
//            window: window
//        })
//        """
//    )

//
//internal fun RequestInit(
//    method: String? = null,
//    headers: dynamic? /* Headers|JsArray<JsArray<JsString>>|OpenEndedDictionary<JsString> */ = null,
//    body: dynamic? /* Blob|BufferSource|FormData|URLSearchParams|String */ = null,
//    referrer: String? = null,
//    referrerPolicy: dynamic? = null,
//    mode: RequestMode? = null,
//    credentials: RequestCredentials? = null,
//    cache: RequestCache? = RequestCache.DEFAULT,
//    redirect: RequestRedirect? = RequestRedirect.FOLLOW,
//    integrity: String? = null,
//    keepalive: Boolean? = null,
//    window: dynamic? = null
//): RequestInit {
//    js(
//        """
//        const obj = {};
//
//        if (method) obj.method = method;
//        if (headers) obj.headers = headers;
//        if (body) obj.body = body;
//        if (referrer) obj.referrer = referrer;
//        if (referrerPolicy) obj.referrerPolicy = referrerPolicy;
//        if (mode) obj.mode = mode;
//        if (credentials) obj.credentials = credentials;
//        if (cache) obj.cache = cache;
//        if (redirect) obj.redirect = redirect;
//        if (integrity) obj.integrity = integrity;
//        if (keepalive) obj.keepalive = keepalive;
//        if (window) obj.window = window;
//
//        return obj;
//        """)
//}
