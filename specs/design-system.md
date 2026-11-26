---
type: Specification
title: Design system
description: "Colours, type, shapes, motion, components."
tags: [design, system, design-system]
stage: 1
mockups: [Home.png]
---

# Design system (`ui/theme/`, `ui/components/`)

Target 1080p = 960×540 dp. Overscan-safe padding 48dp horizontal, 27dp vertical.

## Screen sizes
- **One canvas, scaled to the screen.** Every screen is laid out for 960×540 dp (`KsCanvas` in `ui/theme/Theme.kt`). A TV does not tell apps its size in inches, and its density varies by maker and by the user's Display size setting (a 1080p TV may report 213, 240 or 320), so `KsTheme` sets the density from the window: pixels per dp = min(width / 960, height / 540). On a standard Google TV (720p at 213, 1080p at 320, 4K at 640) that is exactly the system's density, so nothing changes there. On any other density the layout still fills the screen instead of shrinking into a corner or clipping. A window that is not 16:9 fills its width or height and keeps the extra space on the other side, so layouts use `fillMaxSize` and weights, never the screen's pixel size.
- **Text scale** follows the system's font size setting up to 1.3× (Android TV's "Largest"). A quarter of the increase goes back to the layout as room: at 1.3× the canvas is 7.5 % larger in dp (1032×580), so Home's rows still fit and text is still about 21 % larger on screen. Widths that hold text are set from the text size (for example the Memory & storage labels, in caption ems), not in dp, because Android grows small sp more than large sp.
- **Tuned for 55 and 65 inch.** The canvas and the type scale are chosen for 55 and 65 inch TVs watched from a sofa 2–3 m away, the owner's setup (a 55 inch TV):

  | TV | 1 dp on the panel | Body 12sp | Caption 9sp | Distance for a 30° view |
  |---|---|---|---|---|
  | 43 inch | 1.0 mm | 12 mm | 9 mm | 1.8 m |
  | 50 inch | 1.2 mm | 14 mm | 10 mm | 2.1 m |
  | **55 inch** | **1.3 mm** | **15 mm** | **11 mm** | **2.3 m** |
  | **65 inch** | **1.5 mm** | **18 mm** | **14 mm** | **2.7 m** |
  | 75 inch | 1.7 mm | 21 mm | 16 mm | 3.1 m |
  | 85 inch | 2.0 mm | 24 mm | 18 mm | 3.5 m |
  | 100 inch | 2.3 mm | 28 mm | 21 mm | 4.1 m |

  Every size shows the same layout; it only gets physically larger with the screen. A smaller TV seen from the same sofa shows smaller text, so 9sp is the floor and anything that must be read at a glance (titles, the clock, tile names) is 12sp or more.
- **Tested** on one emulator per size, 32 to 100 inch, at 720p, 1080p and 4K, plus the Display size and font size settings (`adb shell wm density`, `settings put system font_scale 1.3`). See the suite's `specs/testing.md`, "TV screen sizes".

## Colours
- Background: vertical gradient `#0C1426` → `#05070D`.
- Glass: fill `Color.White.copy(alpha = 0.12f)`, 1dp border `Color.White.copy(alpha = 0.18f)`.
- Text: primary `#F4F6FB`, secondary `#B8C1D3`, tertiary `#AEB8CB`.
- Warning `#FFD9A3` on `#FFC46E` at 14 %. Success `#A8F0C8`. Danger `#FFB3B3`.
- Primary button: `#F4F6FB` fill, `#0A0F1C` text.

## Type (Inter)
Inter v4.1 is bundled in the APK (`res/font/inter.ttf`): one variable font, weights 400–800, optical size 14, cut down with fonttools to Latin (U+0020–017F), punctuation, currency (₹), letterlike symbols, arrows, math signs and geometric shapes, about 130 KB. Its SIL Open Font License ships as `assets/licenses/Inter-OFL.txt`. Characters outside the cut (Devanagari for Hindi, other alphabets in names) fall back to the system fonts. When text gains a new symbol, check it is in the font or add its range and cut the font again.
Hero title 44sp ExtraBold (−1sp tracking) · Screen title 24sp ExtraBold · Clock 20sp Bold · Section label 11sp SemiBold · Body 12sp · Caption 9–10sp (minimum).

## Shapes and motion
- Radius: tiles 11dp, cards 12dp, dialogs/panels 16dp, pills fully rounded.
- Base durations: focus 200ms `FastOutSlowInEasing`; wallpaper crossfade 400ms; panel slide 280ms. Animator scale 0 → no animation.
- **Transition speed** ([settings](settings.md)) multiplies every duration: 0.25×, 0.5×, 0.75×, 1×, 1.25×, 1.5×. **Default 0.5×** (focus 100ms, panel 140ms, crossfade 200ms): the owner found 1× too slow on the TV, where even the release build draws at about 14 frames a second. Reduce motion still turns all animation off.
- Motion is provided with `compositionLocalOf`, not `staticCompositionLocalOf`: changing Reduce motion or the speed must recompose only what animates, never the whole screen (a whole-screen recomposition froze the TV and made the Reduce motion toggle seem to ignore presses).

## Blur budget
Blur only static layers: the top-bar weather pill, dialogs and side panels (Haze, API 31+). The dynamic wallpaper is a bitmap blurred once, not a live blur. Tiles and cards in scrolling rows and grids use the translucent fill only; never blur behind a scrolling row.

## Components
`GlassCard`, `GlassButton`, `GlassPill`, `GlassDialog`, `GlassSection`, `GlassToggle`, `GlassStepper`, `AppTile`, `DpadFocusable`. Focus behaviour: [focus-navigation](focus-navigation.md).
