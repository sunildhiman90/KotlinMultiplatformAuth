package com.sunildhiman90.kmauth.google

actual object KMAuthGoogle {
    actual val googleAuthManager: GoogleAuthManager
        get() = GoogleAuthManagerAndroid()
}