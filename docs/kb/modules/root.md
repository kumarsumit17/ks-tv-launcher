---
type: Module
title: root
package: com.ks.tvlauncher
spec: [../../spec/app-config.md, ../../spec/architecture.md, ../../spec/focus-navigation.md, ../../spec/app-mode.md]
---

# root

## Purpose
App entry: `KsApplication` (holds the lazy `AppContainer`), `AppContainer` (manual DI, including the Coil `ImageLoader`), `MainActivity` (single activity; app mode: Back on the main screen moves KS to the background).

## Rules
- No work in `Application.onCreate`; everything in `AppContainer` is lazy.
- `MAIN` + `LEANBACK_LAUNCHER` only; never `HOME`/`DEFAULT` ([app-mode](../../spec/app-mode.md)).
- Back with nothing open calls `moveTaskToBack(true)`; overlays close first through their own back handlers.
- No Hilt.

<!-- kb:generated:start -->

## Files (generated)
- `AppContainer.kt` — `AppContainer`
- `KsApplication.kt` — `KsApplication`
- `MainActivity.kt` — `MainActivity`

Tests: `HomeIntentTest.kt`

## Depends on (generated)
- [data.apps](data-apps.md) via `AppArtworkFetcher`, `AppArtworkKeyer`, `AppsRepository`, `PinnedAppsRepository`, `WallpaperSource`
- [data.profile](data-profile.md) via `ProfileRepository`
- [data.settings](data-settings.md) via `SettingsRepository`
- [data.subscriptions](data-subscriptions.md) via `SubscriptionDataStore`, `SubscriptionRepository`
- [data.system](data-system.md) via `DeviceInfoRepository`
- [ui.apps](ui-apps.md) via `AppIntents`, `LocalAppImageLoader`
- [ui.home](ui-home.md) via `HomeActions`, `HomeScreen`, `HomeViewModel`
- [ui.theme](ui-theme.md) via `KsTheme`

## Used by (generated)
- [ui.apps](ui-apps.md) via `KsApplication`
- [ui.home](ui-home.md) via `KsApplication`
- [ui.subscriptions](ui-subscriptions.md) via `KsApplication`

## Spec
- [app-config](../../spec/app-config.md), [architecture](../../spec/architecture.md), [focus-navigation](../../spec/focus-navigation.md), [app-mode](../../spec/app-mode.md)

<!-- kb:generated:end -->
