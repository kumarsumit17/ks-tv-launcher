---
type: Feature
title: Dynamic wallpaper
stage: 2
mockups: [Flow.png]
---

# Dynamic wallpaper

1. A tile gains focus → wait 250 ms (debounce; skipped during fast scrolling).
2. Load that app's **own banner** (else icon) via [apps](apps.md) artwork, scaled up, blurred (Haze/RenderEffect on API 31+, heavily downscaled bitmap below), tinted with its Palette colour.
3. Cancel the previous load; crossfade 400ms.
4. Keep the left-to-right and bottom dark gradient overlays from the Home mockup so text stays readable.
5. Fallback: the plain background gradient.

Toggle in [settings](settings.md). Never use hardcoded web URLs or third-party artwork.
