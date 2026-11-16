---
type: Feature
title: Apps, pinned row and drawer
stage: 2
mockups: [Home.png, AllApps.png]
---

# Apps (`data/apps/`)

## Discovery
`queryIntentActivities` for `LEANBACK_LAUNCHER` and `LAUNCHER`, merged by package (prefer leanback). Exclude `com.ks.tvlauncher`. Expose `Flow<List<AppInfo>>`, refreshed by `LauncherApps.Callback` on install/update/remove.

## Artwork
`ActivityInfo.loadBanner(pm)` → `ApplicationInfo.loadBanner(pm)` → icon. Icon-only apps: icon centred on a frosted tile. Decode off the main thread; memory-cache via Coil keys (package + versionCode).

## Launch
`getLeanbackLaunchIntentForPackage` ?: `getLaunchIntentForPackage`, `FLAG_ACTIVITY_NEW_TASK`. Missing app → glass snackbar + refresh.

## Pinned apps
Ordered package list in DataStore. First-run defaults: installed ones among Netflix, Prime Video, YouTube, JioHotstar, Spotify, Plex, Kodi (by package name). Drop uninstalled packages silently.

## All apps drawer
Full-screen overlay: search field (IME on OK), category filter All/Video/Music/Games/Tools from `ApplicationInfo.category` (hide empty ones), A–Z toggle, 6-column `LazyVerticalGrid`. Long-press OK → glass menu: Pin/Unpin, Move left/right (pinned), App info (`ACTION_APPLICATION_DETAILS_SETTINGS`), Uninstall (`ACTION_DELETE`). Long-press is never the only way: an "Edit" toggle in the drawer header makes OK open the same menu, and the Home hero has the "Unpin" button.
