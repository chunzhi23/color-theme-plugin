val kspVersion: String by project

plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}

group = "io.github.chunzhi23"
version = "1.0.5"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:kotlinpoet:2.2.0")
    implementation("com.squareup:kotlinpoet-ksp:2.2.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:${kspVersion}")
}

tasks.test {
    useJUnitPlatform()
}

fun property(key: String): String = project.extra[key] as String

// gradle.properties
val authorName = property("author.username")
val authorEmail = property("author.email")
val projectName = property("project.name")

// ~/.gradle/gradle.properties
val gprUser: String = property("gpr.user")
val gprKey: String = property("gpr.key")
val pgpKeyId: String = property("signing.keyId")
val pgpPassword: String = property("signing.password")
val pgpSecretKey: String = property("signing.secretKey")

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

tasks.named("assemble") {
    dependsOn(sourcesJar, javadocJar)
}

publishing {
    publications {
        create<MavenPublication>("processor") {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            artifactId = "color-theme"

            pom {
                val projectUrl = "github.com/$authorName/$projectName"

                url.set("https://$projectUrl")
                name.set(projectName)
                description.set("Kotlin annotation processor to generate getters for color themes")

                developers {
                    developer {
                        name.set(authorName)
                        email.set(authorEmail)
                    }
                }

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/mit-license.php")
                    }
                }

                scm {
                    url.set("https://$projectUrl/tree/main")
                    connection.set("scm:git:git://$projectUrl.git")
                    developerConnection.set("scm:git:ssh://$projectUrl.git")
                }
            }
        }
    }

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

signing {
    useInMemoryPgpKeys(pgpKeyId, pgpSecretKey, pgpPassword)
    sign(publishing.publications["processor"])
}
