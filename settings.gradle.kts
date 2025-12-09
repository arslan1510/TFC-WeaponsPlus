pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral() {
            name = "MavenCentral"
        }
        maven {
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net/")
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = "NeoForge"
                    url = uri("https://maven.neoforged.net/releases")
                }
            }
            filter { includeGroupAndSubgroups("net.neoforged") }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}