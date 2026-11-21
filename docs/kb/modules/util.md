---
type: Module
title: util
package: com.ks.tvlauncher.util
spec: [../../../specs/home-screen.md, ../../../specs/performance.md]
---

# util

## Purpose
Small helpers: `minuteTicker` (flow on `ACTION_TIME_TICK`) and `DateTimeFormats` (locale time and date patterns).

## Rules
- No polling; receivers are registered only while collected.

<!-- kb:generated:start -->

## Files (generated)
- `util/DateTimeFormats.kt` — `DateTimeFormats`
- `util/MinuteTicker.kt` — `minuteTicker`

## Depends on (generated)
- nothing inside the app

## Used by (generated)
- [data.reminders](data-reminders.md) via `DateTimeFormats`
- [ui.home](ui-home.md) via `DateTimeFormats`, `minuteTicker`
- [ui.subscriptions](ui-subscriptions.md) via `DateTimeFormats`, `minuteTicker`

## Spec
- [home-screen](../../../specs/home-screen.md), [performance](../../../specs/performance.md)

<!-- kb:generated:end -->
