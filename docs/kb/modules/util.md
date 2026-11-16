---
type: Module
title: util
package: com.ks.tvlauncher.util
spec: [../../spec/home-screen.md, ../../spec/performance.md]
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
- [ui.home](ui-home.md) via `DateTimeFormats`, `minuteTicker`

## Spec
- [home-screen](../../spec/home-screen.md), [performance](../../spec/performance.md)

<!-- kb:generated:end -->
