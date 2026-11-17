---
type: Module
title: domain
package: com.ks.tvlauncher.domain
spec: [../../spec/subscriptions.md, ../../spec/greeting-widget.md, ../../spec/apps.md, ../../spec/inputs.md, ../../spec/custom-wallpaper.md]
---

# domain

## Purpose
Pure Kotlin rules with unit tests: greeting period, profile-name rules, model-name tidying, the `AppInfo` model, pinned-app rules (`PinnedApps`) and the drawer's search/filter/sort (`AppFilter`). Stage 3-4 add `SubscriptionCalculator`, `ByteFormatter` (decimal GB/MB, 75 % / 90 % levels), `WeatherCodes` (WMO → 6 icon groups, °C/°F) and `WeatherCity`; `ProfileNames` gains add/rename/remove.

## Rules
- No Android or Compose imports.
- Every public function here has a unit test.
- Money is `Long` minor units everywhere; yearly → monthly is half-up to the minor unit.
- `nextRenewal` steps whole cycles from the stored date (no month-end drift).
- `SubscriptionCsv` is a contract with ks-expense-manager; `SubscriptionCsvTest` locks it. Change only with agreement and update both repos' docs.
- `Money` groups INR itself (lakh/crore): the JVM's `NumberFormat` cannot, so tests and devices would differ.
- `RenewalReminders` (Stage 5): renewals 0..lead days ahead of active subscriptions; each `id@date` key is notified once; a key that no longer matches the current renewal is dropped and its notification cancelled.
- `TvInputs` (Stage 6a): passthrough inputs only; label = custom ?: own ?: id (blank counts as unset); order by kind (HDMI first), then input id with numbers compared by value.
- `Wallpapers` (Stage 6b): a missing mode follows the old dynamic switch (off → PLAIN); a damaged image reads as built-in 0; `fitWithin` never scales up and never returns 0.
- `AnimationSpeed`: steps 25-150 %, default 50; odd stored values snap to the nearest step; a scaled duration is never 0.
- `PinnedApps.DEFAULT_CANDIDATES` holds real package names only; check a new one exists before adding it.

<!-- kb:generated:start -->

## Files (generated)
- `domain/AnimationSpeed.kt` — `AnimationSpeed`
- `domain/AppFilter.kt` — `AppFilter`
- `domain/AppInfo.kt` — `AppCategory`, `AppInfo`
- `domain/ByteFormatter.kt` — `ByteFormatter`, `UsageLevel`
- `domain/DeviceNames.kt` — `DeviceNames`
- `domain/Greeting.kt` — `Greeting`, `GreetingPeriod`
- `domain/Money.kt` — `Money`
- `domain/PinnedApps.kt` — `PinnedApps`
- `domain/ProfileNames.kt` — `ProfileNames`
- `domain/RenewalReminders.kt` — `DueReminder`, `ReminderPlan`, `RenewalReminders`
- `domain/Subscription.kt` — `BillingCycle`, `Subscription`
- `domain/SubscriptionCalculator.kt` — `NextRenewal`, `SubscriptionCalculator`, `SubscriptionSummary`
- `domain/SubscriptionCsv.kt` — `SubscriptionCsv`
- `domain/TvInputs.kt` — `InputConnection`, `InputKind`, `TvInput`, `TvInputSource`, `TvInputs`
- `domain/Wallpapers.kt` — `WallpaperDim`, `WallpaperImage`, `WallpaperMode`, `Wallpapers`
- `domain/Weather.kt` — `TemperatureUnit`, `WeatherCity`, `WeatherCityMode`, `WeatherCodes`, `WeatherGroup`

Tests: `domain/AnimationSpeedTest.kt`, `domain/AppFilterTest.kt`, `domain/ByteFormatterTest.kt`, `domain/DeviceNamesTest.kt`, `domain/GreetingTest.kt`, `domain/MoneyTest.kt`, `domain/PinnedAppsTest.kt`, `domain/ProfileNamesEditTest.kt`, `domain/ProfileNamesTest.kt`, `domain/RenewalRemindersTest.kt`, `domain/SubscriptionCalculatorTest.kt`, `domain/SubscriptionCsvTest.kt`, `domain/TvInputsTest.kt`, `domain/WallpapersTest.kt`, `domain/WeatherCodesTest.kt`

## Depends on (generated)
- nothing inside the app

## Used by (generated)
- [data.apps](data-apps.md) via `AppCategory`, `AppInfo`, `PinnedApps`
- [data.profile](data-profile.md) via `ProfileNames`
- [data.reminders](data-reminders.md) via `BillingCycle`, `DueReminder`, `Money`, `RenewalReminders`
- [data.settings](data-settings.md) via `AnimationSpeed`, `Money`, `RenewalReminders`, `TemperatureUnit`, `WallpaperDim`, `WallpaperImage` +4
- [data.subscriptions](data-subscriptions.md) via `Subscription`
- [data.system](data-system.md) via `DeviceNames`, `InputConnection`, `InputKind`, `TvInput`, `TvInputSource`, `TvInputs`
- [data.wallpaper](data-wallpaper.md) via `Wallpapers`
- [data.weather](data-weather.md) via `WeatherCity`, `WeatherCityMode`
- [ui.apps](ui-apps.md) via `AppCategory`, `AppFilter`, `AppInfo`
- [ui.components](ui-components.md) via `Wallpapers`
- [ui.controlcenter](ui-controlcenter.md) via `InputConnection`, `TvInput`, `WallpaperMode`, `Wallpapers`
- [ui.home](ui-home.md) via `AppInfo`, `DeviceNames`, `Greeting`, `GreetingPeriod`, `Money`, `PinnedApps` +8
- [ui.settings](ui-settings.md) via `AnimationSpeed`, `AppInfo`, `ProfileNames`, `RenewalReminders`, `TemperatureUnit`, `WallpaperDim` +5
- [ui.subscriptions](ui-subscriptions.md) via `BillingCycle`, `Money`, `NextRenewal`, `Subscription`, `SubscriptionCalculator`, `SubscriptionCsv` +1
- [ui.theme](ui-theme.md) via `AnimationSpeed`
- [ui.widgets](ui-widgets.md) via `ByteFormatter`, `GreetingPeriod`, `UsageLevel`

## Spec
- [subscriptions](../../spec/subscriptions.md), [greeting-widget](../../spec/greeting-widget.md), [apps](../../spec/apps.md), [inputs](../../spec/inputs.md), [custom-wallpaper](../../spec/custom-wallpaper.md)

<!-- kb:generated:end -->
