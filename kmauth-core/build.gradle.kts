@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    id("module.publication")
}

kotlin {

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    jvm()
    android {
        namespace = "com.sunildhiman90.kmauth.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources { enable = true }
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
        packaging {
            resources {
                excludes.add("/META-INF/AL2.0")
                excludes.add("/META-INF/LGPL2.1")
            }
        }
    }
    iosArm64()
    iosSimulatorArm64()

    js(IR) {
        nodejs()
        browser()
        binaries.library()
    }

    wasmJs {
        nodejs()
        browser()
        binaries.library()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                //Kermit
                api(libs.kermit)
            }
        }
        val commonTest by getting {
            dependencies {

            }
        }
    }
}
