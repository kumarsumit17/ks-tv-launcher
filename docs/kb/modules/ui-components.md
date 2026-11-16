---
type: Module
title: ui.components
package: com.ks.tvlauncher.ui.components
spec: [../../spec/design-system.md, ../../spec/focus-navigation.md]
---

# ui.components

## Purpose
Reusable glass building blocks and the single focus treatment (`animateDpadFocus`, `DpadFocusable`).

## Rules
- Every focusable control uses `animateDpadFocus`.
- `frost()` only on still surfaces, never inside scrolling rows or grids.
- A long-press action always has a second path.

<!-- kb:generated:start -->

## Files (generated)
- `ui/components/AppTile.kt` — `AppTile`, `EmptyAppTile`
- `ui/components/DpadFocus.kt` — `DpadFocusable`, `animateDpadFocus`
- `ui/components/Frost.kt` — `FrostSource`, `frost`
- `ui/components/Glass.kt` — `glass`
- `ui/components/GlassButton.kt` — `GlassButton`, `GlassIconButton`
- `ui/components/GlassCard.kt` — `GlassCard`
- `ui/components/GlassDialog.kt` — `GlassDialog`
- `ui/components/GlassPill.kt` — `GlassPill`, `PillTone`
- `ui/components/GlassSection.kt` — `GlassSection`
- `ui/components/GlassStepper.kt` — `GlassStepper`
- `ui/components/GlassToggle.kt` — `GlassToggle`

## Depends on (generated)
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.theme](ui-theme.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `LocalGlassStyle`, `LocalMotion`

## Used by (generated)
- [ui.home](ui-home.md) via `EmptyAppTile`, `FrostSource`, `GlassIconButton`, `GlassPill`, `glass`
- [ui.widgets](ui-widgets.md) via `GlassCard`, `GlassPill`

## Spec
- [design-system](../../spec/design-system.md), [focus-navigation](../../spec/focus-navigation.md)

<!-- kb:generated:end -->
