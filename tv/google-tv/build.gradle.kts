import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.baselineprofile)
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
        // Plain vX.Y.Z or this platform's google-tv-vX.Y.Z (the suite's platform tags).
        ?: git("describe", "--tags", "--abbrev=0", "--match", "v*", "--match", "google-tv-v*")
            ?.removePrefix("google-tv-")?.removePrefix("v")
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
        // In-app updates (specs/app-updates.md): only the signed release can be updated by
        // a release, so debug (KS TV Dev) and perf never check.
        buildConfigField("boolean", "IN_APP_UPDATES", "false")
    }

    // Only the app's own languages from libraries' resources (specs/performance.md).
    androidResources {
        localeFilters += listOf("en", "hi")
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
        // KS TV Dev (specs/dev-workflow.md): debug builds are a separate app, so testing
        // never touches the real com.ks.tvlauncher or its data. Label and banner tint come from
        // src/debug/res. The namespace stays com.ks.tvlauncher.
        debug {
            applicationIdSuffix = ".dev"
        }
        release {
            buildConfigField("boolean", "IN_APP_UPDATES", "true")
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
        // Smoothness measurements only (specs/smoothness.md): release code (minified, not
        // debuggable) installed as KS TV Dev with the debug key. Never released.
        create("perf") {
            initWith(getByName("release"))
            applicationIdSuffix = ".dev"
            buildConfigField("boolean", "IN_APP_UPDATES", "false")
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    // perf shares KS TV Dev's label and banner tint with debug.
    sourceSets.getByName("perf").res.directories.add("src/debug/res")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// The baseline-profile plugin adds release-like build types for generating and measuring
// (benchmarkRelease, nonMinifiedRelease). They install as KS TV Dev and never check for updates,
// so they can never replace or touch the real app (specs/dev-workflow.md).
androidComponents {
    onVariants { variant ->
        val type = variant.buildType.orEmpty()
        if (type.startsWith("benchmark") || type.startsWith("nonMinified")) {
            variant.applicationId.set("com.ks.tvlauncher.dev")
            variant.buildConfigFields?.put(
                "IN_APP_UPDATES",
                com.android.build.api.variant.BuildConfigField("boolean", "false", null),
            )
        }
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
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.tv.material)
    implementation(libs.haze)
    implementation(libs.haze.blur)
    implementation(libs.coil.compose)
    implementation(libs.androidx.palette)
    // Renewal reminders (Stage 5): one daily check, no service.
    implementation(libs.androidx.work.runtime)

    // With the serialization compiler plugin for @Serializable models (subscriptions).
    implementation(libs.kotlinx.serialization.json)

    // Generic utilities shared across KS-Suite apps (git submodule ks-utility/).
    implementation(libs.ks.utility)

    // Installs the shipped baseline profile on devices without Play's cloud profiles.
    implementation(libs.androidx.profileinstaller)
    // The start-up and Home profile, generated by :tv:google-tv-baselineprofile.
    baselineProfile(project(":tv:google-tv-baselineprofile"))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}

// ---- Language files (ks-utility; specs/languages.md) -----------------------------------------------
// The English and Hindi text lives in src/main/lang; ks-utility's tool turns it, with the shared
// catalogue, into values/lang_strings.xml and values-hi/lang_strings.xml (git-ignored). Needs Node.
val langTool = rootProject.file("ks-utility/typescript/tools/lang.mjs")
val langDir = file("src/main/lang")
val checkLanguageFiles by tasks.registering(Exec::class) {
    group = "verification"
    description = "Checks the English and Hindi language files."
    inputs.dir(langDir)
    inputs.dir(rootProject.file("ks-utility/lang"))
    outputs.upToDateWhen { false }
    commandLine("node", langTool, "check", langDir)
}
val languageResources by tasks.registering(Exec::class) {
    description = "Turns the language files into Android string resources."
    inputs.dir(langDir)
    inputs.dir(rootProject.file("ks-utility/lang"))
    outputs.files(file("src/main/res/values/lang_strings.xml"), file("src/main/res/values-hi/lang_strings.xml"))
    commandLine("node", langTool, "android", langDir, "--out", file("src/main/res"))
}
tasks.named("preBuild") { dependsOn(languageResources) }
// The language-file check runs with the unit tests (CI runs testDebugUnitTest) and with check.
tasks.withType<Test>().configureEach { dependsOn(checkLanguageFiles) }
tasks.named("check") { dependsOn(checkLanguageFiles) }

// Compose compiler reports for the smoothness pass (specs/smoothness.md):
// ./gradlew assemblePerf -PcomposeReports  → tv/google-tv/build/compose-reports
if (providers.gradleProperty("composeReports").isPresent) {
    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose-reports")
        metricsDestination = layout.buildDirectory.dir("compose-reports")
    }
}
