pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ks-tv-launcher"
include(":app")

// Shared generic utilities (git submodule). Gradle builds it from source and substitutes it
// for the "com.ks:utility" dependency, so that dependency has no version.
includeBuild("ks-utility/kotlin")
