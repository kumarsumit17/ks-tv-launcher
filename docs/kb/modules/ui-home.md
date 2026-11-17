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
- Until `appsLoaded`, the hero shows "Loading your apps…" and the All apps card "Loading…" (count `null`), never the empty state. The Subscriptions card shows "Loading…" until `SubscriptionsUiState.loaded`.
- Initial focus: the first pinned tile once apps have loaded, else the All apps card; always via `requestFocusSafely`.
- Vertical order is fixed with `focusProperties` on each tile and button: hero buttons ↔ pinned row ↔ bottom strip. `FocusMemory` sends focus back to the tile focused last.
- Reopening KS (`reopenEvents`) closes overlays, keeps the row position and refocuses the last tile. There is no Home-key logic.
- The hero follows the last focused pinned tile; the wallpaper follows the hero after a 250 ms debounce.
- Each new app list writes first-run pins once and prunes uninstalled ones (in `HomeViewModel`).
- After the app menu closes, `HomeScreen` refocuses the tile it was opened from; do not move that into the dialog.
- Bottom strip: All apps card and Subscriptions card side by side; both send Up to the last pinned tile. OK on the Subscriptions card opens `SubscriptionsScreen` over Home; Back or reopening closes it and focus returns to the card.
- UI state is immutable and collected with lifecycle; upstream work stops 5 s after the screen is hidden.
- Bottom strip (Stage 4): All apps · Subscriptions · Memory & storage (hideable) · Control Center; every card sends Up to the last pinned tile.
- The gear opens KS Settings; the weather pill opens Settings at Weather. Android settings are reached from Settings or Control Center.
- `homeMemory` remembers the Home control focused last (`trackFocus`); `restoreKey` snapshots it when Control Center, Settings or the first-run dialog opens, because focus briefly lands on another Home control while an overlay closes (seen on the TV). Closing calls `restoreHomeFocus()` once the overlay has gone and no other overlay took over.
- Hold OK on non-tile controls and MENU open Control Center only when no overlay is open (`openControlCenter`).
- First run: the profile dialog opens after initial focus has settled, never on top of another overlay.

<!-- kb:generated:start -->

## Files (generated)
- `ui/home/AllAppsCard.kt` — `AllAppsCard`
- `ui/home/HomeBackground.kt` — `HomeBackground`
- `ui/home/HomeHero.kt` — `HomeHero`
- `ui/home/HomeScreen.kt` — `HomeActions`, `HomeScreen`
- `ui/home/HomeTopBar.kt` — `HomeTopBar`
- `ui/home/HomeViewModel.kt` — `HomeMessage`, `HomeUiState`, `HomeViewModel`
- `ui/home/PinnedRow.kt` — `PinnedRow`
- `ui/home/SubscriptionsCard.kt` — `SubscriptionsCard`
- `ui/home/WeatherPill.kt` — `WeatherPill`, `groupIcon`, `groupLabel`

## Depends on (generated)
- [data.apps](data-apps.md) via `AppsRepository`, `PinnedAppsRepository`, `Wallpaper`, `WallpaperSource`
- [data.profile](data-profile.md) via `Profile`, `ProfileRepository`
- [data.settings](data-settings.md) via `LauncherSettings`, `SettingsRepository`
- [data.subscriptions](data-subscriptions.md) via `SubscriptionRepository`
- [data.system](data-system.md) via `DeviceInfo`, `DeviceInfoRepository`, `DeviceStats`, `DeviceStatsRepository`
- [data.weather](data-weather.md) via `CityResolver`, `ResolvedCity`, `WeatherRepository`, `WeatherState`
- [domain](domain.md) via `AppInfo`, `DeviceNames`, `Greeting`, `GreetingPeriod`, `Money`, `PinnedApps` +4
- [root](root.md) via `KsApplication`
- [ui.apps](ui-apps.md) via `AppArtworkImage`, `AppIntents`, `AppMenu`, `AppMenuState`, `AppsDrawer`, `AppsDrawerViewModel`
- [ui.components](ui-components.md) via `AppTile`, `FocusMemory`, `FrostSource`, `GlassButton`, `GlassCard`, `GlassIconButton` +5
- [ui.controlcenter](ui-controlcenter.md) via `ControlCenter`, `ControlCenterActions`, `ControlCenterCard`, `ControlCenterValues`, `ControlCenterViewModel`, `SystemIntents`
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.settings](ui-settings.md) via `ProfileDialog`, `SettingsScreen`, `SettingsSection`, `SettingsViewModel`
- [ui.subscriptions](ui-subscriptions.md) via `SubscriptionsScreen`, `SubscriptionsUiState`, `SubscriptionsViewModel`, `nextRenewalLine`, `subscriptionsState`
- [ui.theme](ui-theme.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `LocalGlassStyle`, `LocalMotion` +1
- [ui.widgets](ui-widgets.md) via `GreetingWidget`, `MemoryStorageCard`, `MemoryStoragePanel`
- [util](util.md) via `DateTimeFormats`, `minuteTicker`

## Used by (generated)
- [root](root.md) via `HomeActions`, `HomeScreen`, `HomeViewModel`

## Spec
- [home-screen](../../spec/home-screen.md), [focus-navigation](../../spec/focus-navigation.md), [dynamic-wallpaper](../../spec/dynamic-wallpaper.md), [app-mode](../../spec/app-mode.md)

<!-- kb:generated:end -->
