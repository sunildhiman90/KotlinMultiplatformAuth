@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.kotlinxSerialization)
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

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    android {
        namespace = "com.sunildhiman90.kmauth.google"
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

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {}

    cocoapods {
        ios.deploymentTarget = "15.0"

        framework {
            // Required properties
            // Framework name configuration. Use this property instead of deprecated 'frameworkName'
            baseName = "kmauth_google"

            // Optional properties
            // Specify the framework linking type. It's dynamic by default.
            isStatic = true
        }

        //We can use this library in iosMain,
        // Also we need to add GoogleSignIn in ios xcode project iosApp either by cocoapods or spm
        pod("GoogleSignIn")
    }

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
                api(projects.kmauthCore)
                // Kotlinx Serialization
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.serialization.json)

                // ktor
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }

        androidMain.dependencies {
            //for android google sign in using CredentialManager
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
        }

        jvmMain.dependencies {

            //google sign in
            implementation(libs.google.api.client)
            implementation(libs.google.oauth.client)
            implementation(libs.google.http.client.gson)

            // Ktor for HTTP server
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.client.cio)
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

// Workaround for Xcode 16/27+ dropping support for iOS deployment targets < 15.0 (KT-57741 raised to 12.0 only)
tasks.matching { it.name == "podGenIos" }.configureEach {
    doLast {
        val podfile = layout.buildDirectory.file("cocoapods/synthetic/ios/Podfile").get().asFile
        if (podfile.exists()) {
            val content = podfile.readText()
            val updated = content
                .replace(
                    "config.build_settings['CODE_SIGNING_ALLOWED'] = \"NO\"",
                    "config.build_settings['CODE_SIGNING_ALLOWED'] = \"NO\"\n      config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '15.0'"
                )
                .replace("#{12}.#{0}", "#{15}.#{0}")
                .replace("< 12", "< 15")
                .replace("== 12", "== 15")
            podfile.writeText(updated)
        }
    }
}



