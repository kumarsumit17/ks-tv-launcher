---
type: Module
title: ui.home
package: com.ks.tvlauncher.ui.home
spec: [../../spec/home-screen.md, ../../spec/focus-navigation.md, ../../spec/dynamic-wallpaper.md, ../../spec/app-mode.md]
---

# ui.home

## Purpose
The main screen: layout and overlays (`HomeScreen`, `HomeActions`), `HomeViewModel`/`HomeUiState`, top bar, hero, pinned row, All apps card and the wallpaper background.

## Rules
- Initial focus: the first pinned tile once apps have loaded, else the All apps card; always via `requestFocusSafely`.
- Vertical order is fixed with `focusProperties` on each tile and button: hero buttons ↔ pinned row ↔ bottom strip. `FocusMemory` sends focus back to the tile focused last.
- Reopening KS (`reopenEvents`) closes overlays, keeps the row position and refocuses the last tile. There is no Home-key logic.
- The hero follows the last focused pinned tile; the wallpaper follows the hero after a 250 ms debounce.
- Each new app list writes first-run pins once and prunes uninstalled ones (in `HomeViewModel`).
- After the app menu closes, `HomeScreen` refocuses the tile it was opened from; do not move that into the dialog.
- UI state is immutable and collected with lifecycle; upstream work stops 5 s after the screen is hidden.

<!-- kb:generated:start -->

## Files (generated)
- `ui/home/AllAppsCard.kt` — `AllAppsCard`
- `ui/home/HomeBackground.kt` — `HomeBackground`
- `ui/home/HomeHero.kt` — `HomeHero`
- `ui/home/HomeScreen.kt` — `HomeActions`, `HomeScreen`
- `ui/home/HomeTopBar.kt` — `HomeTopBar`
- `ui/home/HomeViewModel.kt` — `HomeMessage`, `HomeUiState`, `HomeViewModel`
- `ui/home/PinnedRow.kt` — `PinnedRow`

## Depends on (generated)
- [data.apps](data-apps.md) via `AppsRepository`, `PinnedAppsRepository`, `Wallpaper`, `WallpaperSource`
- [data.profile](data-profile.md) via `Profile`, `ProfileRepository`
- [data.settings](data-settings.md) via `LauncherSettings`, `SettingsRepository`
- [data.system](data-system.md) via `DeviceInfo`, `DeviceInfoRepository`
- [domain](domain.md) via `AppInfo`, `DeviceNames`, `Greeting`, `GreetingPeriod`, `PinnedApps`, `ProfileNames`
- [root](root.md) via `KsApplication`
- [ui.apps](ui-apps.md) via `AppArtworkImage`, `AppIntents`, `AppMenu`, `AppMenuState`, `AppsDrawer`, `AppsDrawerViewModel`
- [ui.components](ui-components.md) via `AppTile`, `FocusMemory`, `FrostSource`, `GlassButton`, `GlassCard`, `GlassIconButton` +4
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.theme](ui-theme.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `LocalGlassStyle`, `LocalMotion` +1
- [ui.widgets](ui-widgets.md) via `GreetingWidget`
- [util](util.md) via `DateTimeFormats`, `minuteTicker`

## Used by (generated)
- [root](root.md) via `HomeActions`, `HomeScreen`, `HomeViewModel`

## Spec
- [home-screen](../../spec/home-screen.md), [focus-navigation](../../spec/focus-navigation.md), [dynamic-wallpaper](../../spec/dynamic-wallpaper.md), [app-mode](../../spec/app-mode.md)

<!-- kb:generated:end -->
