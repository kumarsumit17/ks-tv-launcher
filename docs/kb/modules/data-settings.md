---
type: Module
title: data.settings
package: com.ks.tvlauncher.data.settings
spec: [../../spec/settings.md, ../../spec/performance.md]
---

# data.settings

## Purpose
Every launcher setting as one `LauncherSettings` flow plus setters (24-hour clock, show date, wallpaper mode/image/dim, reduce motion, glass intensity, currency, Memory card, weather city, °C/°F).

## Rules
- One DataStore file (`ks_launcher`) shared with profiles; add new keys here with defaults, never a second DataStore.
- Wallpaper (Stage 6b): `wallpaper_mode` replaces `dynamic_wallpaper`, which is read only to migrate "off" to PLAIN and removed by any mode or image write. `setWallpaperImage` also sets CUSTOM. `onWallpaperPictureFailed` changes nothing unless that same picture is still the CUSTOM choice. Any new choice clears `wallpaper_picture_failed`.
- `animation_speed_percent` (default 50, normalised on read and write) is the Transition speed.
- `currency` (ISO 4217, default INR) feeds subscriptions and the CSV; picked in Settings.
- `remindersEnabled` (default false) and `reminderLeadDays` (1-14, default 3, clamped on read and write) drive [data.reminders](data-reminders.md).
- `autoUpdateCheck` (`update_auto_check`, default true) drives [data.updates](data-updates.md); writing it never touches reminders.
- `weatherCityMode` defaults to AUTO; `setManualCity` writes the city and MANUAL in one edit; switching back to AUTO keeps the manual city. `weatherCity` is JSON; a damaged value reads as "no city", never an error.

<!-- kb:generated:start -->

## Files (generated)
- `data/settings/SettingsRepository.kt` — `GlassIntensity`, `LauncherSettings`, `SettingsRepository`

Tests: `data/settings/SettingsRepositoryTest.kt`

## Depends on (generated)
- [domain](domain.md) via `AnimationSpeed`, `Money`, `RenewalReminders`, `TemperatureUnit`, `WallpaperDim`, `WallpaperImage` +4

## Used by (generated)
- [data.reminders](data-reminders.md) via `SettingsRepository`
- [data.weather](data-weather.md) via `LauncherSettings`
- [root](root.md) via `SettingsRepository`
- [ui.home](ui-home.md) via `LauncherSettings`, `SettingsRepository`
- [ui.settings](ui-settings.md) via `GlassIntensity`, `LauncherSettings`, `SettingsRepository`
- [ui.subscriptions](ui-subscriptions.md) via `SettingsRepository`
- [ui.theme](ui-theme.md) via `GlassIntensity`

## Spec
- [settings](../../spec/settings.md), [performance](../../spec/performance.md)

<!-- kb:generated:end -->
