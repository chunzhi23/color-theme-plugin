val kspVersion: String by project

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("signing")
}

group = "io.github.chunzhi23"
version = "1.1.3"

if (hasProperty("release")) {
    version = (version as String).removeSuffix("-SNAPSHOT")
}

if (hasProperty("versionTag")) {
    val tag = properties["versionTag"] as String
    version = (version as String).replace("-SNAPSHOT", "-$tag")
    logger.lifecycle("Building with version: $version")
}

if (hasProperty("releaseVersion")) {
    version = properties["releaseVersion"] as String
}

fun prop(key: String) = project.extra[key] as String
val authorName = prop("author.username")
val projectName = prop("project.name")
val gprUser = prop("gpr.user")
val gprKey = prop("gpr.key")

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$authorName/$projectName")
            credentials {
                username = gprUser
                password = gprKey
            }
        }
        mavenLocal()
    }
}

repositories {
    mavenCentral()
}

val emptyJar by tasks.registering(Jar::class) {
    archiveAppendix.set("empty")
}

kotlin {
    jvm {
        mavenPublication {
            groupId = group as String
            pom { name = "${project.name}-jvm" }

            artifact(emptyJar) {
                classifier = "javadoc"
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    metadata()

    sourceSets {
        val commonMain by getting
        val commonTest by getting

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("com.squareup:kotlinpoet:2.2.0")
                implementation("com.squareup:kotlinpoet-ksp:2.2.0")
                implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
        val jvmTest by getting
    }
}

publishing {
    publications {
        configureEach {
            if (this is MavenPublication) {
                pom.config()
            }
        }
    }
}

typealias MavenPomFile = MavenPom

fun MavenPomFile.config(config: MavenPomFile.() -> Unit = {}) {
    config()

    name = project.name
    url = "https://github.com/$authorName/$projectName"
    description = "Kotlin annotation processor to generate getters for color themes"

    licenses {
        license {
            name = "The Apache License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
        }
    }

    scm {
        connection = "scm:git:git@github.com:$authorName/$projectName.git"
        url = "https://github.com/$authorName/$projectName"
        tag = "HEAD"
    }

    developers {
        developer {
            name = "Lee Jongmin"
            organization = "Hongryeo"
            email = "lzhongm05@gmail.com"
        }
    }
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = true
}

val pgpKeyId = prop("signing.keyId")
val pgpPassword = prop("signing.password")
val pgpSecretKey = prop("signing.secretKey")

signing {
    useInMemoryPgpKeys(pgpKeyId, pgpSecretKey, pgpPassword)
    sign(publishing.publications)
}
