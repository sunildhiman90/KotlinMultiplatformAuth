package com.sunildhiman90.kmauth.google

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

//This way only working in js, but not in wasm, May be some cors issue or may be something else.
suspend fun getGoogleUserInfo(accessToken: String): GoogleUser {
    val url = "https://www.googleapis.com/oauth2/v2/userinfo"
    val client = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                },
            )
        }
    }
    val response = client.get(url) {
        headers {
            append("Authorization", "Bearer $accessToken")
            append(HttpHeaders.Accept, "application/json")
        }
    }
    return response.body<GoogleUser>()
}
