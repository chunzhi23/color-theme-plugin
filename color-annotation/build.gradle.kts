import java.util.Properties

val secretFile = rootProject.file("secret.properties")
if (!secretFile.exists()) {
    throw GradleException()
}
val secretProps = Properties().apply {
    load(secretFile.inputStream())
}
val gprUser: String = secretProps.getProperty("gpr.user")
val gprKey: String = secretProps.getProperty("gpr.key")

val githubName: String by project
val githubRepo: String by project

val kspVersion: String by project

plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "org.hongryeo"
version = "1.0.0"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.devtools.ksp:symbol-processing-api:${kspVersion}")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("annotation") {
            from(components["java"])
            artifactId = "color-annotation"
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubName/$githubRepo")
            credentials {
                username = gprUser
                password = gprKey
            }
        }
        mavenLocal()
    }
}
