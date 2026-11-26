// Baseline-profile generator and start-up benchmarks for the Google TV app (specs/performance.md).
// Test only: nothing here ships. Runs against KS TV Dev builds (com.ks.tvlauncher.dev), never the
// real app. Generate with an emulator connected:
//   ./gradlew :tv:google-tv:generateReleaseBaselineProfile
// Measure start-up:
//   ./gradlew :tv:google-tv-baselineprofile:connectedBenchmarkReleaseAndroidTest
plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.ks.tvlauncher.baselineprofile"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // The TV-size emulators are where start-up is compared (with and without the profile);
        // absolute numbers come from the owner's TV.
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    targetProjectPath = ":tv:google-tv"
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
