---
type: Specification
title: Settings
description: "Every persisted setting."
tags: [settings, screen]
stage: 4
---

# Settings (all persisted in DataStore, all D-pad reachable)

- Profile: names, active name, "Use my own TV name" ([greeting-widget](greeting-widget.md))
- Show Memory & storage card on/off
- Wallpaper mode (Dynamic / Custom / Plain), built-in or picked image, dim ([custom-wallpaper](custom-wallpaper.md)); glass intensity (3 levels); reduce motion; transition speed (stepper, 0.25×–1.5×, default 0.5×, [design-system](design-system.md))
- Clock 12/24h; show/hide date
- Weather city: automatic by default (detected from the network), with "Change city" for a manual city and "Use my current city (automatic)" to go back; "Detect again"; °C/°F ([weather](weather.md))
- Pinned apps order
- Currency
- Renewal reminder lead time (Stage 5, [reminders](reminders.md))
- About: version name/code from `BuildConfig`
- Stage 6: Wallpaper mode (Dynamic / Custom / Plain), image choice and dim ([custom-wallpaper](custom-wallpaper.md)); in About, "Check for updates", automatic checks on/off, and the update flow ([app-updates](app-updates.md))
