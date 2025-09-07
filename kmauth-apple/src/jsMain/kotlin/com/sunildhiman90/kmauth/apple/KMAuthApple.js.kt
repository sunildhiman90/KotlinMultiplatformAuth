package com.sunildhiman90.kmauth.apple


actual object KMAuthApple {
    actual val appleAuthManager: AppleAuthManager
        get() = AppleAuthManagerJs()
}