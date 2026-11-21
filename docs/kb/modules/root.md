---
type: Module
title: root
package: com.ks.tvlauncher
spec: [../../../specs/app-config.md, ../../../specs/architecture.md, ../../../specs/focus-navigation.md, ../../../specs/app-mode.md]
---

# root

## Purpose
App entry: `KsApplication` (holds the lazy `AppContainer`), `AppContainer` (manual DI, including the Coil `ImageLoader`), `MainActivity` (single activity; app mode: Back on the main screen moves KS to the background).

## Rules
- No work in `Application.onCreate`; everything in `AppContainer` is lazy.
- `MAIN` + `LEANBACK_LAUNCHER` only; never `HOME`/`DEFAULT` ([app-mode](../../../specs/app-mode.md)).
- Back with nothing open calls `moveTaskToBack(true)`; overlays close first through their own back handlers.
- No Hilt.
- `KsApplication` is WorkManager's `Configuration.Provider` (on-demand start); `AppContainer` holds `reminderStore` and `reminderScheduler`.
- `AppContainer.ensureDailyWork()` runs once per process (guarded by an `AtomicBoolean`), from `MainActivity` on `Dispatchers.Default` about 5 s after the activity is first resumed. It registers what `DailyWorkPlan` returns, then `UpdateManager.cleanUpAfterStart()` runs.

<!-- kb:generated:start -->

## Files (generated)
- `AppContainer.kt` — `AppContainer`
- `KsApplication.kt` — `KsApplication`
- `MainActivity.kt` — `MainActivity`

Tests: `HomeIntentTest.kt`

## Depends on (generated)
- [data.apps](data-apps.md) via `AppArtworkFetcher`, `AppArtworkKeyer`, `AppsRepository`, `PinnedAppsRepository`, `WallpaperSource`
- [data.profile](data-profile.md) via `ProfileRepository`
- [data.reminders](data-reminders.md) via `ReminderScheduler`, `ReminderStore`
- [data.settings](data-settings.md) via `SettingsRepository`
- [data.subscriptions](data-subscriptions.md) via `SubscriptionDataStore`, `SubscriptionRepository`
- [data.system](data-system.md) via `DeviceInfoRepository`, `DeviceStatsRepository`, `SystemStatusRepository`, `TvInputRepository`
- [data.updates](data-updates.md) via `GitHubUpdateSource`, `UpdateChecker`, `UpdateManager`, `UpdateNotifier`, `UpdateScheduler`, `UpdateStore`
- [data.wallpaper](data-wallpaper.md) via `WallpaperPictureStore`
- [data.weather](data-weather.md) via `CityResolver`, `IpWhoIsLocator`, `OpenMeteoProvider`, `WeatherRepository`
- [domain](domain.md) via `DailyWork`, `DailyWorkPlan`
- [ui.apps](ui-apps.md) via `LocalAppImageLoader`
- [ui.home](ui-home.md) via `HomeActions`, `HomeScreen`, `HomeViewModel`
- [ui.theme](ui-theme.md) via `KsTheme`

## Used by (generated)
- [data.reminders](data-reminders.md) via `KsApplication`, `MainActivity`
- [data.updates](data-updates.md) via `KsApplication`, `MainActivity`
- [ui.apps](ui-apps.md) via `KsApplication`
- [ui.controlcenter](ui-controlcenter.md) via `KsApplication`
- [ui.home](ui-home.md) via `KsApplication`
- [ui.settings](ui-settings.md) via `KsApplication`
- [ui.subscriptions](ui-subscriptions.md) via `KsApplication`

## Spec
- [app-config](../../../specs/app-config.md), [architecture](../../../specs/architecture.md), [focus-navigation](../../../specs/focus-navigation.md), [app-mode](../../../specs/app-mode.md)

<!-- kb:generated:end -->
