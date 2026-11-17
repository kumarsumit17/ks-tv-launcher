---
type: Configuration
title: App configuration
stage: 1
---

# App configuration

## Identity
App name `KS - TV Launcher`. `namespace` = `applicationId` = `com.ks.tvlauncher`.

## Versioning (`app/build.gradle.kts`, never hardcoded)
- `versionCode` = `git rev-list --count HEAD`; `versionName` = `git describe --tags --abbrev=0`.
- Run git via `providers.exec { … }` (configuration-cache safe), wrapped in `runCatching`.
- Fallbacks: `versionCode = 1`, `versionName = "0.1.0-dev"`.
- `-PversionCode=` / `-PversionName=` (or env `VERSION_CODE` / `VERSION_NAME`) override all. Comment: CI should use `fetch-depth: 0`.

## Manifest
- `MainActivity`: exported, `launchMode="singleTask"`, `stateNotNeeded="true"`, landscape, `resizeableActivity="false"`.
- Intent filter: `MAIN` + `LEANBACK_LAUNCHER` only. No `HOME`/`DEFAULT`: KS runs as an app, never the default Home ([app-mode](app-mode.md)).
- Labels: application `android:label` = "KS - TV Launcher"; `MainActivity` `android:label` = "KS TV" (short, for launcher tiles).
- `android.software.leanback` required; `android.hardware.touchscreen` `required="false"`.
- `android:banner`: 320×180 px xhdpi vector/layer-list, "KS" on a navy gradient. Adaptive app icon.
- `<queries>`: one intent for `MAIN` + `LEANBACK_LAUNCHER`, one for `MAIN` + `LAUNCHER`. **No** `QUERY_ALL_PACKAGES`.
- Permissions: `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, and `REQUEST_DELETE_PACKAGES` (lets the app menu's Uninstall open the system uninstall dialog; required since API 28). Stage 4: `BLUETOOTH` with `maxSdkVersion="30"` (install-time; Control Center on/off) plus `android.hardware.bluetooth` `required="false"`, and `PACKAGE_USAGE_STATS` (never requested at runtime; the user may grant Usage access for the optional Apps/Other storage split). No `BLUETOOTH_CONNECT` and no location permission. `POST_NOTIFICATIONS` only in Stage 5 ([reminders](reminders.md)).
