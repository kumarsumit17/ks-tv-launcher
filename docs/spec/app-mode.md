---
type: Decision
title: App mode (not the default Home)
stage: 2
---

# App mode: KS is an app, never the default Home

The owner keeps their own ad-free launcher (`com.sumit.lumiohome`) as the TV's Home and opens KS from its favourites row. KS must never become, or offer to become, the Home app.

## Rules
- **Manifest**: `MainActivity` has `MAIN` + `LEANBACK_LAUNCHER` only. **No** `HOME` and no `DEFAULT` category, and no activity-alias that adds them ([app-config](app-config.md)).
- **Back** on the KS main screen with nothing open: `moveTaskToBack(true)`, which returns to the owner's launcher and keeps KS warm for a fast reopen. Back still closes overlays first ([focus-navigation](focus-navigation.md)).
- **Home key**: handled by the system (goes to the owner's launcher). KS adds no Home-key logic.
- **Reopening from favourites** while KS is in the background (`singleTask`, `onNewIntent`): close any open overlay and keep the pinned-row position. If a screen or overlay was open (Subscriptions, Settings, the drawer, a menu or dialog), focus returns to the control that opened it, for example the Subscriptions card. With nothing open, focus returns to the last focused tile.
- **Tile label**: the activity label is the short "KS TV" (`app_name_short`), so it fits launcher tiles. The application label and all in-app text stay "KS - TV Launcher".
- Everything else (pinned apps, drawer, launching apps, wallpaper, widgets, subscriptions, Control Center, weather) works unchanged.

## Verify
- `adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME` still prints the owner's launcher, and Android never shows a Home-app chooser because of KS.
- The tile in the owner's launcher reads "KS TV" without clipping.
