plugins {
    id("java-library")
    kotlin("jvm") version "2.4.21"
    id("com.google.devtools.ksp") version "2.3.10" apply false
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
                from(components["java"])
            }
        }
    }
}