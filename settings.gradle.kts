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

rootProject.name = "ColorThemePlugin"

include("color-theme")
