---
type: Module
title: data.apps
package: com.ks.tvlauncher.data.apps
spec: [../../../specs/apps.md, ../../../specs/dynamic-wallpaper.md, ../../../specs/performance.md]
---

# data.apps

## Purpose
Installed apps and everything read from them: `AppsRepository` (discovery, launch intents), `PinnedAppsRepository` (ordered pins in DataStore), the Coil artwork pipeline (`AppArtwork`, keyer, fetcher) and `WallpaperSource` (tiny tinted bitmap for the dynamic wallpaper).

## Rules
- No polling: the app list refreshes only from `LauncherApps.Callback`, registered while someone collects.
- `apps` is null until the first scan; null is not the same as an empty list.
- Pins: `pinned` is null only before first-run defaults are written. Never re-apply defaults after that.
- Artwork comes only from the app's own banner or icon via `PackageManager`. No web URLs, no disk cache.
- Artwork cache key is package + versionCode + kind; keep `AppArtworkKeyer` in step with `AppArtwork`.
- No `QUERY_ALL_PACKAGES`; visibility comes from the manifest `<queries>`.

<!-- kb:generated:start -->

## Files (generated)
- `data/apps/AppArtwork.kt` — `AppArtwork`, `AppArtworkFetcher`, `AppArtworkKeyer`, `artwork`
- `data/apps/AppListCache.kt` — `AppListCache`
- `data/apps/AppsRepository.kt` — `AppsRepository`
- `data/apps/PinnedAppsRepository.kt` — `PinnedAppsRepository`
- `data/apps/WallpaperSource.kt` — `Wallpaper`, `WallpaperSource`

Tests: `data/apps/AppArtworkKeyerTest.kt`, `data/apps/PinnedAppsRepositoryTest.kt`

## Depends on (generated)
- [domain](domain.md) via `AppCategory`, `AppInfo`, `AppListCodec`, `PinnedApps`

## Used by (generated)
- [root](root.md) via `AppArtworkFetcher`, `AppArtworkKeyer`, `AppsRepository`, `PinnedAppsRepository`, `WallpaperSource`
- [ui.apps](ui-apps.md) via `AppsRepository`, `PinnedAppsRepository`, `artwork`
- [ui.home](ui-home.md) via `AppsRepository`, `PinnedAppsRepository`, `Wallpaper`, `WallpaperSource`
- [ui.settings](ui-settings.md) via `AppsRepository`, `PinnedAppsRepository`

## Spec
- [apps](../../../specs/apps.md), [dynamic-wallpaper](../../../specs/dynamic-wallpaper.md), [performance](../../../specs/performance.md)

<!-- kb:generated:end -->
