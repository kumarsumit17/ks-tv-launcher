---
type: Feature
title: Custom wallpaper
stage: 6
---

# Custom wallpaper

A background the user picks, next to the existing [dynamic-wallpaper](dynamic-wallpaper.md).

## Setting `wallpaperMode`
- `DYNAMIC` (default): the focused app's artwork, as today. Replaces the old dynamic wallpaper on/off switch; migrate "off" to `PLAIN`.
- `CUSTOM`: the chosen image, always.
- `PLAIN`: the navy gradient only.

## Choosing an image ([settings](settings.md) › Wallpaper)
- **Built-in set**: 6 wallpapers drawn in code (gradients and soft shapes in the design-system palette). No bundled photos, no downloads, nothing third-party.
- **From a file**: `ACTION_OPEN_DOCUMENT` with `image/*` (USB drive or the TV's storage). Show this row only if an activity resolves the intent; many TVs have no file picker.
- The picked image is decoded once, downscaled to at most 1920×1080, and copied to `filesDir/wallpaper.jpg`. KS keeps no permission on the original and never reads it again. "Remove image" deletes the copy.

## Display
- Drawn full screen behind Home with the same left-to-right and bottom dark overlays as the dynamic wallpaper, so text stays readable; a "Dim" setting with 3 levels.
- Loaded once through Coil at screen size and kept while Home is visible; no live blur ([design-system](design-system.md) blur budget).
- If the file is missing or unreadable, fall back to `PLAIN` and say so in Settings.
- The copy lives in app storage, so it survives updates ([app-updates](app-updates.md)) and is removed on uninstall.
