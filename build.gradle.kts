import me.modmuss50.mpp.ReleaseType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import net.fabricmc.loom.task.RemapJarTask

fun getBranch(): String = System.getenv("GITHUB_REF")?.substringAfterLast('/') ?: "main"

plugins {
    id("java")
    id("fabric-loom")
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    id("me.modmuss50.mod-publish-plugin") version "0.8.1"
}

version = project.property("mod_version") as String
group = project.property("package") as String

base {
    archivesName.set(project.property("mod_id") as String)
}

repositories {
    // ... your repositories
}

loom {
    splitEnvironmentSourceSets()

    mods {
        create(project.property("mod_id") as String) {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("fabric_kotlin_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version") as String)
    inputs.property("loader_version", project.property("loader_version") as String)

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to project.version,
                "minecraft" to project.property("minecraft_version"),
                "fabricloader" to project.property("loader_version"),
                "mod_id" to project.property("mod_id"),
                "mod_name" to project.property("mod_name"),
                "github_repo" to project.property("github_repo")
            )
        )
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

publishMods {
    val remapJar = tasks.named<RemapJarTask>("remapJar").get()
    file.set(remapJar.archiveFile)
    changelog.set(System.getenv("CHANGELOG") ?: "No changelog provided")
    type.set(ReleaseType.STABLE)
    displayName.set("Yas Zoom ${project.version}")
    modLoaders.add("fabric")
    dryRun.set(System.getenv("GITHUB_TOKEN") == null)

    modrinth {
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        projectId.set(project.property("modrinth_id") as String)
        minecraftVersions.add(project.property("minecraft_version") as String)
        projectDescription.set(providers.fileContents(layout.projectDirectory.file("README.md")).asText)
    }

    github {
        accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
        if (System.getenv("CI") != null) {
            repository.set(providers.environmentVariable("GITHUB_REPOSITORY"))
        } else {
            repository.set(project.property("github_repo") as String)
        }
        commitish.set(if (System.getenv("CI") != null) getBranch() else "main")
    }
}