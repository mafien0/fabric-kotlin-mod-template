pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // The version is hardcoded here because of limitations in the Gradle Kotlin DSL
        // when trying to read properties files in the pluginManagement block.
        id("fabric-loom") version "1.13-SNAPSHOT"
    }
}
