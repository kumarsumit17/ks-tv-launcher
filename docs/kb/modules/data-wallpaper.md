---
type: Module
title: data.wallpaper
package: com.ks.tvlauncher.data.wallpaper
spec: [../../../specs/custom-wallpaper.md]
---

# data.wallpaper

## Purpose
The user's wallpaper picture: `WallpaperPictureStore` copies a picked image to `filesDir/wallpaper.jpg` (decoded once, at most 1920×1080), loads it for Home through Coil, removes it, and says whether the TV has a document picker.

## Rules
- Never call `takePersistableUriPermission` or keep the picked `Uri`: the original is read once and forgotten.
- Write to `wallpaper.jpg.tmp` and rename, so a failed import never damages the previous copy.
- `ImageDecoder` (API 28+) decodes straight to the target size and applies the image's rotation; below that, `inSampleSize` then `scale`.
- `load` skips Coil's memory cache: Home's view model holds the only copy and drops it when Home is hidden.
- `canPickFiles` ignores `com.android.tv.frameworkpackagestubs` (a "no app" placeholder); the manifest `<queries>` entry for `OPEN_DOCUMENT image/*` is needed for the lookup.
- Which image is shown, the mode and the failure note live in [data.settings](data-settings.md), not here.

<!-- kb:generated:start -->

## Files (generated)
- `data/wallpaper/WallpaperPictureStore.kt` — `WallpaperPictureStore`

## Depends on (generated)
- [domain](domain.md) via `Wallpapers`

## Used by (generated)
- [root](root.md) via `WallpaperPictureStore`
- [ui.home](ui-home.md) via `WallpaperPictureStore`
- [ui.settings](ui-settings.md) via `WallpaperPictureStore`

## Spec
- [custom-wallpaper](../../../specs/custom-wallpaper.md)

<!-- kb:generated:end -->
