// AGP 9 has built-in Kotlin support, so the org.jetbrains.kotlin.android plugin is not applied.
// The Compose compiler plugin pins the Kotlin version for the build.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
}
