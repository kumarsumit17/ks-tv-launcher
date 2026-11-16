---
type: Module
title: ui.home
package: com.ks.tvlauncher.ui.home
spec: [../../spec/home-screen.md, ../../spec/focus-navigation.md, ../../spec/dynamic-wallpaper.md]
---

# ui.home

## Purpose
The Home screen: layout, `HomeViewModel`/`HomeUiState`, top bar, hero and pinned row.

## Rules
- Initial focus goes to the first tile via `requestFocusSafely`.
- The Home key scrolls the row back and refocuses the first tile.
- UI state is immutable and collected with lifecycle.

<!-- kb:generated:start -->

## Files (generated)
- `ui/home/HomeHero.kt` — `HomeHero`
- `ui/home/HomeScreen.kt` — `HomeScreen`
- `ui/home/HomeTopBar.kt` — `HomeTopBar`
- `ui/home/HomeViewModel.kt` — `HomeUiState`, `HomeViewModel`
- `ui/home/PinnedRow.kt` — `PinnedRow`

## Depends on (generated)
- [data.profile](data-profile.md) via `Profile`, `ProfileRepository`
- [data.settings](data-settings.md) via `LauncherSettings`, `SettingsRepository`
- [data.system](data-system.md) via `DeviceInfo`, `DeviceInfoRepository`
- [domain](domain.md) via `DeviceNames`, `Greeting`, `GreetingPeriod`, `ProfileNames`
- [root](root.md) via `KsApplication`
- [ui.components](ui-components.md) via `EmptyAppTile`, `FrostSource`, `GlassIconButton`, `GlassPill`, `glass`
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.theme](ui-theme.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `VisualSettings`
- [ui.widgets](ui-widgets.md) via `GreetingWidget`
- [util](util.md) via `DateTimeFormats`, `minuteTicker`

## Used by (generated)
- [root](root.md) via `HomeScreen`, `HomeViewModel`

## Spec
- [home-screen](../../spec/home-screen.md), [focus-navigation](../../spec/focus-navigation.md), [dynamic-wallpaper](../../spec/dynamic-wallpaper.md)

<!-- kb:generated:end -->
