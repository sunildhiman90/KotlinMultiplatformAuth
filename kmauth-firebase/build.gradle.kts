import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.sunildhiman90"
version = "0.0.1"

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "com.sunildhiman90.kmauth.firebase"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "kmauth-firebase", version.toString())

    pom {
        name = "KotlinMultiplatformAuth"
        description = "Kotlin Multiplatform Authentication Librar targetting all platforms."
        inceptionYear = "2024"
        url = "https://github.com/sunildhiman90/KotlinMultiplatformAuth/"
        licenses {
            license {
                name = "Apache-2.0"
                url = "https://opensource.org/licenses/Apache-2.0"
            }
        }
        developers {
            developer {
                id = "sunildhiman90"
                name = "Sunil Kumar"
                url = "https://github.com/sunildhiman90"
            }
        }
        scm {
            url = "https://github.com/sunildhiman90/KotlinMultiplatformAuth"
            connection = "https://github.com/sunildhiman90/KotlinMultiplatformAuth.git"
            developerConnection = "https://github.com/sunildhiman90"
        }
    }
}
