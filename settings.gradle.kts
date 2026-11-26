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
// The Google TV app (suite platform folders): tv/google-tv/.
include(":tv:google-tv")
// Baseline-profile generator and start-up benchmarks for the Google TV app (test only, never shipped).
include(":tv:google-tv-baselineprofile")
project(":tv:google-tv-baselineprofile").projectDir = file("tv/google-tv/baselineprofile")

// Shared generic utilities (git submodule). Gradle builds it from source and substitutes it
// for the "com.ks:utility" dependency, so that dependency has no version.
includeBuild("ks-utility/kotlin")
