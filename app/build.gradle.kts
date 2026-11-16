import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// ---- Versioning (never hardcoded) -------------------------------------------------------------
// Priority: -PversionCode / -PversionName, then env VERSION_CODE / VERSION_NAME, then git, then
// the fallbacks. The release workflow passes -P values derived from the tag. CI must check out
// with `fetch-depth: 0` so rev-list and describe see the full history.
fun git(vararg args: String): String? = runCatching {
    providers.exec { commandLine("git", *args) }
        .standardOutput.asText.get().trim()
        .ifEmpty { null }
}.getOrNull()

val appVersionCode: Int =
    providers.gradleProperty("versionCode").orNull?.toIntOrNull()
        ?: providers.environmentVariable("VERSION_CODE").orNull?.toIntOrNull()
        ?: git("rev-list", "--count", "HEAD")?.toIntOrNull()
        ?: 1

val appVersionName: String =
    providers.gradleProperty("versionName").orNull
        ?: providers.environmentVariable("VERSION_NAME").orNull
        ?: git("describe", "--tags", "--abbrev=0")?.removePrefix("v")
        ?: "0.1.0-dev"

// ---- Signing: CI sets SIGNING_* env vars; local release builds use an untracked keystore.properties.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use(::load)
}

// Blank counts as missing: GitHub Actions passes an unset secret as an empty string.
fun signingValue(env: String, key: String): String? =
    providers.environmentVariable(env).orNull?.takeIf { it.isNotBlank() }
        ?: keystoreProperties.getProperty(key)?.takeIf { it.isNotBlank() }

// Only sign with a keystore that really exists and has content. Otherwise the release build
// is left unsigned instead of failing (for example in CI before the signing secrets are set).
val releaseKeystore: File? = signingValue("SIGNING_STORE_FILE", "storeFile")
    ?.let { rootProject.file(it) }
    ?.takeIf { it.isFile && it.length() > 0L }

android {
    namespace = "com.ks.tvlauncher"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.ks.tvlauncher"
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            releaseKeystore?.let { keystore ->
                storeFile = keystore
                storePassword = signingValue("SIGNING_STORE_PASSWORD", "storePassword")
                keyAlias = signingValue("SIGNING_KEY_ALIAS", "keyAlias")
                keyPassword = signingValue("SIGNING_KEY_PASSWORD", "keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Without signing material the release build is left unsigned instead of failing.
            signingConfigs.getByName("release").takeIf { it.storeFile != null }?.let {
                signingConfig = it
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.text.google.fonts)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.tv.material)
    implementation(libs.haze)
    implementation(libs.haze.blur)
    implementation(libs.coil.compose)
    implementation(libs.androidx.palette)

    // Used with built-in serializers only (no compiler plugin needed yet).
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
