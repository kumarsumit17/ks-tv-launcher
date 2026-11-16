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
- Intent filter: `MAIN` + categories `HOME`, `DEFAULT`, `LEANBACK_LAUNCHER`.
- `android.software.leanback` required; `android.hardware.touchscreen` `required="false"`.
- `android:banner`: 320×180 px xhdpi vector/layer-list, "KS" on a navy gradient. Adaptive app icon.
- `<queries>`: one intent for `MAIN` + `LEANBACK_LAUNCHER`, one for `MAIN` + `LAUNCHER`. **No** `QUERY_ALL_PACKAGES`.
- Permissions: `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`. `POST_NOTIFICATIONS` only in Stage 5 ([reminders](reminders.md)).
