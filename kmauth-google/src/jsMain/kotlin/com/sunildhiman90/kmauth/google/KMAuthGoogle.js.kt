package com.sunildhiman90.kmauth.google

actual object KMAuthGoogle {
    actual fun getGoogleAuthManager(): GoogleAuthManager = GoogleAuthManagerJs()
}