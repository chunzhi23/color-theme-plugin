val kspVersion: String by project

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("signing")
}

group = "io.github.chunzhi23"
version = "1.1.3"

/**
 * If "release" profile is used the "-SNAPSHOT" suffix of the version is removed.
 */
if (hasProperty("release")) {
    val versionString = version as String
    if (versionString.endsWith("-SNAPSHOT")) {
        version = versionString.replace("-SNAPSHOT", "")
    }
}

/**
 * Handler of "versionTag" property.
 * Required to support Maven and NPM repositories that doesn't support "-SNAPSHOT" versions. To build and publish
 * artifacts with specific version run "./gradlew -PversionTag=my-tag" and the final version will be "0.6.13-my-tag".
 */
if (hasProperty("versionTag")) {
    val versionString = version as String
    val versionTag = properties["versionTag"]
    if (versionString.endsWith("-SNAPSHOT")) {
        version = versionString.replace("-SNAPSHOT", "-$versionTag")
        logger.lifecycle("Project will be built with version '$version'.")
    } else {
        error("Could not apply 'versionTag' together with non-snapshot version.")
    }
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

    metadata {
        mavenPublication {
            groupId = group as String
            artifactId = "${project.name}-common"
            pom {
                name = "${project.name}-common"
            }
        }
    }

    sourceSets {
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
