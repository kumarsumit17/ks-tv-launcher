---
type: Design System
title: Design system
stage: 1
mockups: [Home.png]
---

# Design system (`ui/theme/`, `ui/components/`)

Target 1080p = 960×540 dp. Overscan-safe padding 48dp horizontal, 27dp vertical.

## Colours
- Background: vertical gradient `#0C1426` → `#05070D`.
- Glass: fill `Color.White.copy(alpha = 0.12f)`, 1dp border `Color.White.copy(alpha = 0.18f)`.
- Text: primary `#F4F6FB`, secondary `#B8C1D3`, tertiary `#AEB8CB`.
- Warning `#FFD9A3` on `#FFC46E` at 14 %. Success `#A8F0C8`. Danger `#FFB3B3`.
- Primary button: `#F4F6FB` fill, `#0A0F1C` text.

## Type (Inter)
Hero title 44sp ExtraBold (−1sp tracking) · Screen title 24sp ExtraBold · Clock 20sp Bold · Section label 11sp SemiBold · Body 12sp · Caption 9–10sp (minimum).

## Shapes and motion
- Radius: tiles 11dp, cards 12dp, dialogs/panels 16dp, pills fully rounded.
- Focus 200ms `FastOutSlowInEasing`; wallpaper crossfade 400ms; panel slide 280ms. Animator scale 0 → no animation.

## Blur budget
Blur only static layers: the top-bar weather pill, dialogs and side panels (Haze, API 31+). The dynamic wallpaper is a bitmap blurred once, not a live blur. Tiles and cards in scrolling rows and grids use the translucent fill only; never blur behind a scrolling row.

## Components
`GlassCard`, `GlassButton`, `GlassPill`, `GlassDialog`, `GlassSection`, `GlassToggle`, `GlassStepper`, `AppTile`, `DpadFocusable`. Focus behaviour: [focus-navigation](focus-navigation.md).
