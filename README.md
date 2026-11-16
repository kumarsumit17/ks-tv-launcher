# KS - TV Launcher

> Part of the [KS-Suite](../README.md).

An ad-free, Apple TV-style home screen for Android TV / Google TV, fully controlled by the remote: glass UI, a greeting widget, pinned apps, a weather pill and a streaming-subscription tracker.

| | |
|---|---|
| **Platform** | Android TV / Google TV (minSdk 26, compileSdk and targetSdk 37) |
| **Tech stack** | Kotlin, Jetpack Compose for TV (`androidx.tv:tv-material`), Preferences DataStore, kotlinx-serialization, Haze |
| **Toolchain** | JDK 17 · Gradle 9.9.1 · AGP 9.5.1 · Kotlin 2.4.21 |

The spec lives in [docs/spec/](docs/spec/index.md). The app is built in stages; see [docs/spec/stages.md](docs/spec/stages.md).

## Getting started

Requirements: JDK 17 and the Android SDK (platform 37, build-tools 36). Android Studio installs both; so do the command-line tools.

```bash
git clone <repo-url> ks-tv-launcher
cd ks-tv-launcher
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Point Gradle at the SDK with an untracked `local.properties` (Android Studio creates it for you):

```properties
sdk.dir=/path/to/android/sdk
```

No API keys are needed. Weather will use Open-Meteo, which has none.

### Setting it as the home app

- **Android TV:** install it and press Home. Pick *KS - TV Launcher* in the chooser, or run `adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME`.
- **Google TV:** most Google TV devices never show a home chooser. See [docs/spec/google-tv-setup.md](docs/spec/google-tv-setup.md).

On-device checks for each stage are in [docs/MANUAL_TEST.md](docs/MANUAL_TEST.md).

## Versioning and release

`versionCode` and `versionName` are never hardcoded. In order of priority they come from `-PversionCode` / `-PversionName`, the `VERSION_CODE` / `VERSION_NAME` environment variables, then git (`rev-list --count HEAD` and the latest tag), then the fallbacks `1` and `0.1.0-dev`.

Releases are cut by pushing a semantic version tag (`git tag v1.2.3 && git push --tags`); the workflow passes the tag's version with `-P`.
See the suite-level [CI/CD guide](../README.md#-cicd-semantic-versioning--automated-signing).

For local release builds, put signing values in an untracked `keystore.properties` using the keys `storeFile`, `storePassword`, `keyAlias` and `keyPassword`. Without signing values a release build is left unsigned.

## License

MIT. See [LICENSE](LICENSE).
