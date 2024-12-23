package com.sunildhiman90.kmauth.firebase

internal class AndroidCorePlatform : CorePlatform {
    override val name: String = "Android"
}

actual fun getCorePlatform(): CorePlatform = AndroidCorePlatform()