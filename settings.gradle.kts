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
    ":kmauth-google-compose",
    ":sample:composeApp"
)

//type safe project accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
