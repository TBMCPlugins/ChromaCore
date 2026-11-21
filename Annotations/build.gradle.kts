plugins {
    id("java-library")
    kotlin("jvm")
    `maven-publish`
}

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

kotlin {
    jvmToolchain(25)
}