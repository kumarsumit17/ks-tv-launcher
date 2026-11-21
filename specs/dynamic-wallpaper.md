---
type: Specification
title: Dynamic wallpaper
description: "Background follows the focused app."
tags: [dynamic, wallpaper, feature]
stage: 2
mockups: [Flow.png]
---

# Dynamic wallpaper

1. A tile gains focus → wait 250 ms (debounce; skipped during fast scrolling).
2. Load that app's **own banner** (else icon) via [apps](apps.md) artwork, scaled up, blurred (Haze/RenderEffect on API 31+, heavily downscaled bitmap below), tinted with its Palette colour.
3. Cancel the previous load; crossfade 400ms.
4. Keep the left-to-right and bottom dark gradient overlays from the Home mockup so text stays readable.
5. Fallback: the plain background gradient.

Toggle in [settings](settings.md); Stage 6 turns the toggle into a wallpaper mode ([custom-wallpaper](custom-wallpaper.md)). Never use hardcoded web URLs or third-party artwork.
