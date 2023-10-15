@file:Suppress("UnstableApiUsage")

import java.net.URI

rootProject.name = "Gymceska"
include(":app")

pluginManagement {
    repositories {
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = URI.create("https://androidx.dev/storage/compose-compiler/repository/")
        }
    }
}