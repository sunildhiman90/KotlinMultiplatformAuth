package com.sunildhiman90.kmauth.core

import platform.Foundation.NSError

fun NSError.toThrowable(): Throwable {
    return Throwable(this.localizedDescription)
}