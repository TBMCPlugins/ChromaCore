plugins {
    id("java-library")
    kotlin("jvm")
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("io.papermc.paper:paper-api:26.3.build.+")
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.6")

    testImplementation(kotlin("test"))
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

kotlin {
    jvmToolchain(25)
}