package com.sunildhiman90.kmauth.google.jsUtils

import com.sunildhiman90.kmauth.google.externals.CredentialResponse
import com.sunildhiman90.kmauth.google.externals.GoogleIdConfiguration
import com.sunildhiman90.kmauth.google.externals.GoogleUserJs
import com.sunildhiman90.kmauth.google.externals.GsiButtonConfiguration
import com.sunildhiman90.kmauth.google.externals.OverridableTokenClientConfig
import com.sunildhiman90.kmauth.google.externals.TokenClientConfig
import com.sunildhiman90.kmauth.google.externals.TokenResponse
import org.w3c.fetch.Headers
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

internal fun getAuthHeaders(accessToken: String): Headers = js(
    """
    ({
        'Accept': 'application/json',
        'Authorization': 'Bearer ' + accessToken,
    })
    """
)

// Dont use await and  return Promise<JsAny> from js(), it will not work,
// if we want to return something we need to use (), but not curly braces {},
// curly braces {} can be used for multi statement, but () can be used for returning something.
// So () can be used for returning something, so we need to define function and call it directly, so that it will be single line
internal fun fetchGoogleUserInfo(url: String, accessToken: String): JsAny = js(
    """
    (() => {
      try {
        return fetch(url, {
          method: "GET",
          headers: {
            Accept: "application/json", // Tell the server we prefer JSON
            Authorization: "Bearer " + accessToken,
          },
          mode: "cors",
        }).then((response) => {
          if (!response.ok) {
            return null;
          }
          return response.json();
        });
      } catch (error) {
        // Catch any network errors or JSON parsing errors.
        return null;
      }
    })()
    """
)

internal fun convertToGoogleUserInfo(userInfo: JsAny): GoogleUserJs = js(
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


internal fun RequestInit(
    method: String? = null,
    headers: JsAny? /* Headers|JsArray<JsArray<JsString>>|OpenEndedDictionary<JsString> */ = null,
    body: JsAny? /* Blob|BufferSource|FormData|URLSearchParams|String */ = null,
    referrer: String? = null,
    referrerPolicy: JsAny? = null,
    mode: RequestMode? = null,
    credentials: RequestCredentials? = null,
    cache: RequestCache? = null,
    redirect: RequestRedirect? = null,
    integrity: String? = null,
    keepalive: Boolean? = null,
    window: JsAny? = null
): RequestInit {
    js("""
        const obj = {};

        if (method) obj.method = method;
        if (headers) obj.headers = headers;
        if (body) obj.body = body;
        if (referrer) obj.referrer = referrer;
        if (referrerPolicy) obj.referrerPolicy = referrerPolicy;
        if (mode) obj.mode = mode;
        if (credentials) obj.credentials = credentials;
        if (cache) obj.cache = cache;
        if (redirect) obj.redirect = redirect;
        if (integrity) obj.integrity = integrity;
        if (keepalive) obj.keepalive = keepalive;
        if (window) obj.window = window;
        
        return obj;
        """)
}