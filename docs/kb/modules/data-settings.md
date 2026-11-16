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
- `currency` (ISO 4217, default INR) is stored now for subscriptions and the CSV; its picker arrives with Settings in Stage 4.

<!-- kb:generated:start -->

## Files (generated)
- `data/settings/SettingsRepository.kt` — `GlassIntensity`, `LauncherSettings`, `SettingsRepository`

## Depends on (generated)
- [domain](domain.md) via `Money`

## Used by (generated)
- [root](root.md) via `SettingsRepository`
- [ui.home](ui-home.md) via `LauncherSettings`, `SettingsRepository`
- [ui.subscriptions](ui-subscriptions.md) via `SettingsRepository`
- [ui.theme](ui-theme.md) via `GlassIntensity`

## Spec
- [settings](../../spec/settings.md), [performance](../../spec/performance.md)

<!-- kb:generated:end -->
