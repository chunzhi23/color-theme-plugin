plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":color-theme"))
}
