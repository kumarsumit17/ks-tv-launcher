---
type: Module
title: root
package: com.ks.tvlauncher
spec: [../../spec/app-config.md, ../../spec/architecture.md, ../../spec/focus-navigation.md]
---

# root

## Purpose
App entry: `KsApplication` (holds the lazy `AppContainer`), `AppContainer` (manual DI), `MainActivity` (single activity; swallows Back; Home key via `onNewIntent`).

## Rules
- No work in `Application.onCreate`.
- Keep HOME, DEFAULT and LEANBACK_LAUNCHER on `MainActivity`.
- No Hilt.

<!-- kb:generated:start -->

## Files (generated)
- `AppContainer.kt` — `AppContainer`
- `KsApplication.kt` — `KsApplication`
- `MainActivity.kt` — `MainActivity`

Tests: `HomeIntentTest.kt`

## Depends on (generated)
- [data.profile](data-profile.md) via `ProfileRepository`
- [data.settings](data-settings.md) via `SettingsRepository`
- [data.system](data-system.md) via `DeviceInfoRepository`
- [ui.home](ui-home.md) via `HomeScreen`, `HomeViewModel`
- [ui.theme](ui-theme.md) via `KsTheme`

## Used by (generated)
- [ui.home](ui-home.md) via `KsApplication`

## Spec
- [app-config](../../spec/app-config.md), [architecture](../../spec/architecture.md), [focus-navigation](../../spec/focus-navigation.md)

<!-- kb:generated:end -->
