package com.sunildhiman90.kmauth.firebase

interface CorePlatform {
    val name: String
}

expect fun getCorePlatform(): CorePlatform