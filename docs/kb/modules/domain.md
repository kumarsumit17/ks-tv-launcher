---
type: Module
title: domain
package: com.ks.tvlauncher.domain
spec: [../../spec/greeting-widget.md, ../../spec/subscriptions.md]
---

# domain

## Purpose
Pure Kotlin rules with unit tests: greeting period, profile-name rules, model-name tidying. Stage 3-4 add `SubscriptionCalculator` and `ByteFormatter`.

## Rules
- No Android imports.
- Every public function here has a unit test.

<!-- kb:generated:start -->

## Files (generated)
- `domain/DeviceNames.kt` — `DeviceNames`
- `domain/Greeting.kt` — `Greeting`, `GreetingPeriod`
- `domain/ProfileNames.kt` — `ProfileNames`

Tests: `domain/DeviceNamesTest.kt`, `domain/GreetingTest.kt`, `domain/ProfileNamesTest.kt`

## Depends on (generated)
- nothing inside the app

## Used by (generated)
- [data.profile](data-profile.md) via `ProfileNames`
- [data.system](data-system.md) via `DeviceNames`
- [ui.home](ui-home.md) via `DeviceNames`, `Greeting`, `GreetingPeriod`, `ProfileNames`
- [ui.widgets](ui-widgets.md) via `GreetingPeriod`

## Spec
- [greeting-widget](../../spec/greeting-widget.md), [subscriptions](../../spec/subscriptions.md)

<!-- kb:generated:end -->
