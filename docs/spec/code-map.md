---
type: Map
title: Code map
stage: all
---

# Code map

Claude updates this at the end of every stage ([token-budget](token-budget.md)). Read it before searching the code. One line per file: `path` — role; key classes/composables. Group by package. Keep it under ~150 lines.

Last updated: Stage 1. Paths are under `app/src/main/java/com/ks/tvlauncher/` unless they start with another folder.

## Build
- `gradle/libs.versions.toml` — every version and library alias.
- `app/build.gradle.kts` — git-based `versionCode`/`versionName` with `-P` and env overrides; signing from `SIGNING_*` env or `keystore.properties`; SDK levels.
- `app/src/main/AndroidManifest.xml` — HOME + DEFAULT + LEANBACK_LAUNCHER filter, `<queries>`, permissions, banner, no backup.
- `app/src/main/res/` — `drawable/banner.xml` (TV banner), `mipmap-anydpi/ic_launcher.xml` (adaptive icon), `values/font_certs.xml` (Google Fonts provider certificates), `values/strings.xml`.

## Root
- `KsApplication.kt` — `KsApplication`; holds the lazy `container`. No work in `onCreate`.
- `AppContainer.kt` — `AppContainer`; manual DI. Lazy `settingsRepository`, `profileRepository`, `deviceInfoRepository`, all on one Preferences DataStore file (`ks_launcher`).
- `MainActivity.kt` — `MainActivity`; sets `KsTheme` + `HomeScreen`, swallows Back, forwards the Home key from `onNewIntent`, opens Android settings from the gear.

## data
- `data/settings/SettingsRepository.kt` — `LauncherSettings`, `GlassIntensity`, `SettingsRepository` (`settings` flow + setters: 24-hour, show date, dynamic wallpaper, reduce motion, glass intensity).
- `data/profile/ProfileRepository.kt` — `Profile`, `ProfileRepository` (`profile` flow, `switchToNext()`); names stored as a JSON array string.
- `data/system/DeviceInfoRepository.kt` — `DeviceInfo`, `DeviceInfoRepository.deviceInfo` (TV name via a content observer, model, Android release).

## domain (pure Kotlin, unit-tested)
- `domain/Greeting.kt` — `GreetingPeriod.at(time)`, `Greeting.displayName`.
- `domain/ProfileNames.kt` — `ProfileNames.sanitize`, `normalize`, `next`; limits 8 names, 20 chars.
- `domain/DeviceNames.kt` — `DeviceNames.tidyModel`, `displayName`.

## util
- `util/MinuteTicker.kt` — `minuteTicker(context)`: flow on `ACTION_TIME_TICK` (no polling).
- `util/DateTimeFormats.kt` — `DateTimeFormats.time`, `shortDate`, `longDate` (locale patterns).

## ui/theme
- `Color.kt` — `KsColors` tokens.
- `Type.kt` — `InterFamily` (downloadable font), `KsType` scale, `KsTypography` for TV Material.
- `Shape.kt` — `KsShapes` (Tile, Card, Panel, Badge, Pill), `KsDimens` (overscan, tile size).
- `Motion.kt` — `KsMotion` durations, `MotionPolicy` + `LocalMotion` (snaps when motion is off), `systemAnimationsEnabled`.
- `Theme.kt` — `KsTheme`, `VisualSettings`, `GlassStyle` + `LocalGlassStyle` (blur radius per intensity, off below API 31).

## ui/icons
- `KsIcons.kt` — `KsIcons`: Gear, Tv, Person, Play, Cloud, Sun, Grid, Sliders, Plus, Minus, ChevronLeft, ChevronRight, Check, Search, Close.

## ui/components
- `DpadFocus.kt` — `Modifier.animateDpadFocus(glow, shape, focusedScale)`, `DpadFocusable`.
- `Glass.kt` — `Modifier.glass(shape, frosted, fill)`: fill + border, optional blur.
- `Frost.kt` — `FrostSource` (Haze source host), `Modifier.frost` (Haze blur, API 31+ only).
- `GlassCard.kt` — `GlassCard` (passive, or a focus stop with `onClick`).
- `GlassButton.kt` — `GlassButton` (primary / glass), `GlassIconButton`.
- `GlassPill.kt` — `GlassPill`, `PillTone`.
- `GlassSection.kt` — `GlassSection` (label + focus group).
- `GlassToggle.kt` — `GlassToggle`.
- `GlassStepper.kt` — `GlassStepper` (press = small step, hold = large step).
- `GlassDialog.kt` — `GlassDialog` (in-tree modal: focus trap, Back closes, returns focus).
- `AppTile.kt` — `AppTile` (160×90, label when focused, artwork slot), `EmptyAppTile`.

## ui/home
- `HomeViewModel.kt` — `HomeUiState`, `HomeViewModel` (`uiState`, `homeKeyEvents`, `switchProfile`), `Factory`.
- `HomeScreen.kt` — `HomeScreen` (layout, initial focus, Home-key reset), `HomeBackground`.
- `HomeTopBar.kt` — `HomeTopBar` (badge, clock, date, weather pill, gear).
- `HomeHero.kt` — `HomeHero` (title + one line).
- `PinnedRow.kt` — `PinnedRow` (lazy row of `EmptyAppTile` slots until Stage 2).

## ui/widgets
- `GreetingWidget.kt` — `GreetingWidget` (greeting + name gradient, long date, TV name · Android version, switch chip).

## Tests
- `app/src/test/.../domain/` — `GreetingTest`, `ProfileNamesTest`, `DeviceNamesTest`.
- `app/src/androidTest/.../HomeIntentTest.kt` — checks the HOME and LEANBACK_LAUNCHER filters on a device.
