package com.sunildhiman90.kmauth.firebase


internal class JSCorePlatform: CorePlatform {
    override val name: String = "Kotlin/Js"
}

actual fun getCorePlatform(): CorePlatform = JSCorePlatform()