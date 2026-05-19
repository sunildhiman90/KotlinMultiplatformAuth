pluginManagement {
    includeBuild("convention-plugins")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "KotlinMultiplatformAuth"
include(
    ":kmauth-core",
    ":kmauth-google",
    ":kmauth-apple",
    ":kmauth-supabase",
    ":kmauth-google-compose",
    ":sample:shared",
    ":sample:androidApp",
    ":sample:desktopApp",
    ":sample:webApp"
)

//type safe project accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
