@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
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

    jvm()
    androidTarget {
        publishAllLibraryVariants()
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {}

    cocoapods {
        ios.deploymentTarget = "13.0"

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
        binaries.executable()
    }

    wasmJs {
        nodejs()
        browser()
        binaries.library()
        binaries.executable()
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



android {
    namespace = "com.sunildhiman90.kmauth.google"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
