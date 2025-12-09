plugins {
    `java-library`
    eclipse
    idea
    `maven-publish`
    id("net.neoforged.gradle.userdev") version "7.0.170"
}

tasks.named<Wrapper>("wrapper").configure {
    distributionType = Wrapper.DistributionType.BIN
}

// Project properties from gradle.properties
val mod_version: String by project
val mod_group_id: String by project
val mod_id: String by project
val mod_name: String by project
val mod_license: String by project
val mod_authors: String by project
val mod_description: String by project
val minecraft_version: String by project
val minecraft_version_range: String by project
val neo_version: String by project
val neo_version_range: String by project
val loader_version_range: String by project
val tfc_file_id: String by project
val patchouli_file_id: String by project

version = mod_version
group = mod_group_id

repositories {
    mavenLocal()
    maven {
        name = "MinecraftForge"
        url = uri("https://maven.minecraftforge.net/")
    }
    maven {
        name = "CurseMaven"
        url = uri("https://cursemaven.com")
    }
}

base {
    archivesName.set(mod_id)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

runs {
    configureEach {
        systemProperty("forge.logging.markers", "REGISTRIES")
        systemProperty("forge.logging.console.level", "debug")
        modSource(sourceSets.main.get())
    }

    register("client") {
        systemProperty("forge.enabledGameTestNamespaces", mod_id)
    }

    register("server") {
        systemProperty("forge.enabledGameTestNamespaces", mod_id)
        programArgument("--nogui")
    }

    register("gameTestServer") {
        systemProperty("forge.enabledGameTestNamespaces", mod_id)
    }

    register("data") {
        programArguments.addAll(
            "--mod",
            mod_id,
            "--all",
            "--output",
            file("src/generated/resources/").absolutePath,
            "--existing",
            file("src/main/resources/").absolutePath
        )
    }
}

sourceSets.main.get().resources.srcDir("src/generated/resources")

// Note: localRuntime configuration is automatically created by NeoGradle 7.0
val localRuntime by configurations.getting
configurations.runtimeClasspath.get().extendsFrom(localRuntime)

dependencies {
    implementation("net.neoforged:neoforge:$neo_version")

    // TFC and Patchouli via CurseMaven
    implementation("curse.maven:terrafirmacraft-302973:$tfc_file_id")
    implementation("curse.maven:patchouli-306770:$patchouli_file_id")
}

tasks.withType<ProcessResources>().configureEach {
    val replaceProperties = mapOf(
        "minecraft_version" to minecraft_version,
        "minecraft_version_range" to minecraft_version_range,
        "neo_version" to neo_version,
        "neo_version_range" to neo_version_range,
        "loader_version_range" to loader_version_range,
        "mod_id" to mod_id,
        "mod_name" to mod_name,
        "mod_license" to mod_license,
        "mod_version" to mod_version,
        "mod_authors" to mod_authors,
        "mod_description" to mod_description
    )
    inputs.properties(replaceProperties)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(replaceProperties)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("file://${project.projectDir}/repo")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

