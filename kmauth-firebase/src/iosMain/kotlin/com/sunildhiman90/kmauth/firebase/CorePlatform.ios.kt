package com.sunildhiman90.kmauth.firebase

import platform.UIKit.UIDevice

internal class IOSCorePlatform: CorePlatform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getCorePlatform(): CorePlatform = IOSCorePlatform()