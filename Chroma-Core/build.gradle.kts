plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.1.0"
    kotlin("jvm")
    id("com.gradleup.shadow") version "9.6.1"
    id("com.google.devtools.ksp") version "2.3.10" apply false
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.essentialsx.net/releases/") // EssentialsX
    maven("https://jitpack.io") // Vault
    maven("https://repo.onarandombox.com/content/groups/public/") // Multiverse-Core
    maven("https://repo.glaremasters.me/repository/towny/") // Towny
    maven("https://libraries.minecraft.net/") // Brigadier
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    compileOnly("net.essentialsx:EssentialsX:2.21.2")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("org.mvplugins.multiverse.core:multiverse-core:5.8.2")
    compileOnly("com.palmergames.bukkit.towny:towny:0.103.2.0")
    implementation("me.lucko:commodore:2.2")
    implementation(project(":ButtonProcessor"))

    testImplementation(kotlin("test"))
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.3:4.119.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    shadowJar {
        dependencies {
            exclude(dependency("com.mojang:brigadier"))
        }

        /* vvv Replace with the package of your plugin vvv */
        relocate("me.lucko.commodore", "buttondevteam.core.commodore")
    }

    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.3")
        jvmArgs("-Xms2G", "-Xmx2G")

        downloadPlugins {
            github("MilkBowl", "Vault", "1.7.3", "Vault.jar")
        }
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

kotlin {
    jvmToolchain(25)
}