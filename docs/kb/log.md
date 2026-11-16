# Code knowledge log

One entry per stage: what changed and why.

## Stage 1
- Foundation, design system, Home shell and greeting widget: 11 modules. Graph and module files seeded from the old code map.

## Stage 2
- App mode: manifest is `MAIN` + `LEANBACK_LAUNCHER` only, the activity label is "KS TV", Back moves the task to the background, and reopening closes overlays and refocuses the last tile.
- New modules `data.apps` (discovery via `LauncherApps.Callback`, pinned list in DataStore, Coil artwork fetcher and keyer, wallpaper source) and `ui.apps` (All apps drawer, app menu, artwork image, app intents).
- Home now shows real pinned apps, a hero with Open and Unpin, the All apps card and the dynamic wallpaper; `EmptyAppTile` is gone.
- `MotionPolicy` returns `FiniteAnimationSpec` so `Crossfade` can use it.
- Added Coil 3 and Palette (first used here) and the `REQUEST_DELETE_PACKAGES` permission, which the Uninstall menu item needs.
- After a first run on the Lumio TV (Android 14): Up/Down rules moved from containers to the items, `focusRestorer` replaced by `FocusMemory`, the wallpaper made smaller, more blurred and much darker, the focus glow smoothed, and full-width buttons now fill their focus ring.
- Same TV session: focus now returns to the acted-on tile after the app menu closes (done from `HomeScreen` once the menu has gone, not from the dialog while it is closing). Verified on the TV: row and drawer focus, menu, pin/unpin/move, search and keyboard, Back chain, background and hot reopen, app launch.
