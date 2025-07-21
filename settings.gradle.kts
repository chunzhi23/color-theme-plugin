pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings

    plugins {
        kotlin("multiplatform") version kotlinVersion
        id("com.google.devtools.ksp") version kspVersion
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "color-theme-plugin"

include("color-theme")
