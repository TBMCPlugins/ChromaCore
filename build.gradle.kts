plugins {
    id("java-library")
    kotlin("jvm") version "2.4.21"
    id("com.google.devtools.ksp") version "2.3.10" apply false
    id("com.gradleup.shadow") version "9.6.1" apply false
    `maven-publish`
}

repositories {
    mavenCentral()
}

subprojects {
    apply<JavaLibraryPlugin>()
    apply<MavenPublishPlugin>()

    publishing {
        publications {
            create<MavenPublication>("maven") {
                afterEvaluate {
                    val shadowJar = tasks.findByName("shadowJar")
                    if (shadowJar == null) from(components["java"])
                    else artifact(shadowJar)
                }
            }
        }
    }
}