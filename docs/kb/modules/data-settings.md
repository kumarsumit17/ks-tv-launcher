---
type: Module
title: data.settings
package: com.ks.tvlauncher.data.settings
spec: [../../spec/settings.md, ../../spec/performance.md]
---

# data.settings

## Purpose
Every launcher setting as one `LauncherSettings` flow plus setters (24-hour clock, show date, dynamic wallpaper, reduce motion, glass intensity).

## Rules
- One DataStore file (`ks_launcher`) shared with profiles; add new keys here with defaults, never a second DataStore.

<!-- kb:generated:start -->

## Files (generated)
- `data/settings/SettingsRepository.kt` — `GlassIntensity`, `LauncherSettings`, `SettingsRepository`

## Depends on (generated)
- nothing inside the app

## Used by (generated)
- [root](root.md) via `SettingsRepository`
- [ui.home](ui-home.md) via `LauncherSettings`, `SettingsRepository`
- [ui.theme](ui-theme.md) via `GlassIntensity`

## Spec
- [settings](../../spec/settings.md), [performance](../../spec/performance.md)

<!-- kb:generated:end -->
