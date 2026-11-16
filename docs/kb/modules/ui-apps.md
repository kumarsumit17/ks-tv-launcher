---
type: Module
title: ui.apps
package: com.ks.tvlauncher.ui.apps
spec: [../../spec/apps.md, ../../spec/focus-navigation.md]
---

# ui.apps

## Purpose
The All apps drawer (`AppsDrawer`, `AppsDrawerViewModel`), the per-app glass menu (`AppMenu`), tile artwork (`AppArtworkImage`, `LocalAppImageLoader`) and the intents for app info, uninstall and safe starting (`AppIntents`).

## Rules
- The drawer is modal and opaque: nothing is blurred behind its scrolling grid.
- Holding OK is never the only way to the menu: the Edit toggle makes OK open it.
- The app menu is drawn inside the drawer's focus group (`overlay`), so focus never leaves the drawer.
- Closing the drawer returns focus to the control that opened it.
- Pinning, moving and launching are done by `HomeViewModel`; the drawer view model only filters.

<!-- kb:generated:start -->

## Files (generated)
- `ui/apps/AppArtworkImage.kt` — `AppArtworkImage`, `LocalAppImageLoader`
- `ui/apps/AppIntents.kt` — `AppIntents`
- `ui/apps/AppMenu.kt` — `AppMenu`, `AppMenuState`
- `ui/apps/AppsDrawer.kt` — `AppsDrawer`
- `ui/apps/AppsDrawerViewModel.kt` — `AppsDrawerUiState`, `AppsDrawerViewModel`, `DrawerApp`

## Depends on (generated)
- [data.apps](data-apps.md) via `AppsRepository`, `PinnedAppsRepository`, `artwork`
- [domain](domain.md) via `AppCategory`, `AppFilter`, `AppInfo`
- [root](root.md) via `KsApplication`
- [ui.components](ui-components.md) via `AppTile`, `FocusMemory`, `GlassButton`, `GlassChip`, `GlassDialog`, `GlassSearchField` +2
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.theme](ui-theme.md) via `KsColors`, `KsDimens`, `KsType`

## Used by (generated)
- [root](root.md) via `AppIntents`, `LocalAppImageLoader`
- [ui.home](ui-home.md) via `AppArtworkImage`, `AppIntents`, `AppMenu`, `AppMenuState`, `AppsDrawer`, `AppsDrawerViewModel`
- [ui.subscriptions](ui-subscriptions.md) via `AppIntents`

## Spec
- [apps](../../spec/apps.md), [focus-navigation](../../spec/focus-navigation.md)

<!-- kb:generated:end -->
