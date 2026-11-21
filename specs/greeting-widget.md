---
type: Specification
title: Greeting widget
description: "Greeting, name profiles, TV name + Android version."
tags: [greeting, widget]
stage: 1 (display), 4 (profile dialog)
mockups: [Greeting.png]
---

# Greeting widget

Glass card at the top-left of [home-screen](home-screen.md), under the top bar.

## Lines
1. "Good morning" (5:00–11:59) / "Good afternoon" (12:00–16:59) / "Good evening" (17:00–4:59) + ", ‹name›". Name in a gradient `#FFE1B0` → `#FFB9C8`. No name saved → greeting only. Updates on the clock's minute ticker.
2. Long date in the TV's locale, e.g. "Monday, 16 November".
3. TV icon + "‹TV name› · Android ‹version›", all detected:
   - TV name: `Settings.Global.getString(cr, Settings.Global.DEVICE_NAME)`, fallback `Build.MODEL`; the user's own name wins if "Use my own TV name" is on.
   - Model: `Build.MANUFACTURER` + `Build.MODEL`, tidied (capitalised, no repeated manufacturer).
   - Version: `"Android ${Build.VERSION.RELEASE}"`; Settings also shows `· API ${Build.VERSION.SDK_INT}`.

## Profiles (who's watching)
- DataStore: ordered names (max 8; trimmed; 1–20 chars) + active name.
- With 2+ names, a "Switch to ‹next›" chip sits at the right of line 3; OK cycles to the next name. It is a focus stop between the top bar and the hero buttons.

## Profile dialog (Stage 4; Settings › Profile, and automatically on first run)
- "Who's watching?" title; name field (opens the IME; voice works through it).
- Chips for saved names (OK selects; long-press OK → Rename / Remove) and "+ Add name".
- "This TV" rows marked AUTO: TV name, Model, Android version; a "Use my own TV name" switch revealing a text field.
- Save / Cancel. Names stay on the device.
