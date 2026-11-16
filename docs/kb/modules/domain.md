---
type: Module
title: domain
package: com.ks.tvlauncher.domain
spec: [../../spec/greeting-widget.md, ../../spec/apps.md, ../../spec/subscriptions.md]
---

# domain

## Purpose
Pure Kotlin rules with unit tests: greeting period, profile-name rules, model-name tidying, the `AppInfo` model, pinned-app rules (`PinnedApps`) and the drawer's search/filter/sort (`AppFilter`). Stage 3-4 add `SubscriptionCalculator` and `ByteFormatter`.

## Rules
- No Android or Compose imports.
- Every public function here has a unit test.
- `PinnedApps.DEFAULT_CANDIDATES` holds real package names only; check a new one exists before adding it.

<!-- kb:generated:start -->

## Files (generated)
- `domain/AppFilter.kt` — `AppFilter`
- `domain/AppInfo.kt` — `AppCategory`, `AppInfo`
- `domain/DeviceNames.kt` — `DeviceNames`
- `domain/Greeting.kt` — `Greeting`, `GreetingPeriod`
- `domain/PinnedApps.kt` — `PinnedApps`
- `domain/ProfileNames.kt` — `ProfileNames`

Tests: `domain/AppFilterTest.kt`, `domain/DeviceNamesTest.kt`, `domain/GreetingTest.kt`, `domain/PinnedAppsTest.kt`, `domain/ProfileNamesTest.kt`

## Depends on (generated)
- nothing inside the app

## Used by (generated)
- [data.apps](data-apps.md) via `AppCategory`, `AppInfo`, `PinnedApps`
- [data.profile](data-profile.md) via `ProfileNames`
- [data.system](data-system.md) via `DeviceNames`
- [ui.apps](ui-apps.md) via `AppCategory`, `AppFilter`, `AppInfo`
- [ui.home](ui-home.md) via `AppInfo`, `DeviceNames`, `Greeting`, `GreetingPeriod`, `PinnedApps`, `ProfileNames`
- [ui.widgets](ui-widgets.md) via `GreetingPeriod`

## Spec
- [greeting-widget](../../spec/greeting-widget.md), [apps](../../spec/apps.md), [subscriptions](../../spec/subscriptions.md)

<!-- kb:generated:end -->
