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
include(":kmauth-core")
include(":kmauth-firebase")
include(":kmauth-google")
include(":sample:composeApp")

//For using library preview in sample
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
