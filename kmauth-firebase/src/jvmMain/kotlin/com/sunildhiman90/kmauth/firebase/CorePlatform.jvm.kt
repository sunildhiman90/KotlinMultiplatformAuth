package com.sunildhiman90.kmauth.firebase

internal class JVMCorePlatform: CorePlatform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getCorePlatform(): CorePlatform = JVMCorePlatform()