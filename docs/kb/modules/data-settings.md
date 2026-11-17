---
type: Module
title: data.settings
package: com.ks.tvlauncher.data.settings
spec: [../../spec/settings.md, ../../spec/performance.md]
---

# data.settings

## Purpose
Every launcher setting as one `LauncherSettings` flow plus setters (24-hour clock, show date, dynamic wallpaper, reduce motion, glass intensity, currency, Memory card, weather city, °C/°F).

## Rules
- One DataStore file (`ks_launcher`) shared with profiles; add new keys here with defaults, never a second DataStore.
- `currency` (ISO 4217, default INR) feeds subscriptions and the CSV; picked in Settings.
- `weatherCityMode` defaults to AUTO; `setManualCity` writes the city and MANUAL in one edit; switching back to AUTO keeps the manual city. `weatherCity` is JSON; a damaged value reads as "no city", never an error.

<!-- kb:generated:start -->

## Files (generated)
- `data/settings/SettingsRepository.kt` — `GlassIntensity`, `LauncherSettings`, `SettingsRepository`

Tests: `data/settings/SettingsRepositoryTest.kt`

## Depends on (generated)
- [domain](domain.md) via `Money`, `TemperatureUnit`, `WeatherCity`, `WeatherCityMode`

## Used by (generated)
- [data.weather](data-weather.md) via `LauncherSettings`
- [root](root.md) via `SettingsRepository`
- [ui.home](ui-home.md) via `LauncherSettings`, `SettingsRepository`
- [ui.settings](ui-settings.md) via `GlassIntensity`, `LauncherSettings`, `SettingsRepository`
- [ui.subscriptions](ui-subscriptions.md) via `SettingsRepository`
- [ui.theme](ui-theme.md) via `GlassIntensity`

## Spec
- [settings](../../spec/settings.md), [performance](../../spec/performance.md)

<!-- kb:generated:end -->
