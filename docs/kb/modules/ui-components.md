---
type: Module
title: ui.components
package: com.ks.tvlauncher.ui.components
spec: [../../spec/design-system.md, ../../spec/focus-navigation.md]
---

# ui.components

## Purpose
Reusable glass building blocks and the single focus treatment (`animateDpadFocus`, `DpadFocusable`), plus focus helpers (`requestFocusSafely`, `modalFocus`).

## Rules
- Every focusable control uses `animateDpadFocus`.
- `frost()` only on still surfaces, never inside scrolling rows or grids.
- A long-press action always has a second path.
- `GlassSearchField` opens the keyboard on OK only, never on focus.
- Overlays use `modalFocus()` and return focus to their opener.
- In lazy rows and grids use `FocusMemory` (a requester per item), not `Modifier.focusRestorer()`: on a real TV the restorer returned focus to the wrong item.
- `focusProperties` on a container does not affect moves that start from its children; set `up`/`down` on the focusable items themselves.

<!-- kb:generated:start -->

## Files (generated)
- `ui/components/AppTile.kt` — `AppTile`
- `ui/components/DpadFocus.kt` — `DpadFocusable`, `animateDpadFocus`
- `ui/components/FocusUtil.kt` — `FocusMemory`, `modalFocus`, `requestFocusSafely`
- `ui/components/Frost.kt` — `FrostSource`, `frost`
- `ui/components/Glass.kt` — `glass`
- `ui/components/GlassButton.kt` — `GlassButton`, `GlassIconButton`
- `ui/components/GlassCard.kt` — `GlassCard`
- `ui/components/GlassChip.kt` — `GlassChip`
- `ui/components/GlassDialog.kt` — `GlassDialog`
- `ui/components/GlassPill.kt` — `GlassPill`, `PillTone`
- `ui/components/GlassSearchField.kt` — `GlassSearchField`
- `ui/components/GlassSection.kt` — `GlassSection`
- `ui/components/GlassSnackbar.kt` — `GlassSnackbar`
- `ui/components/GlassStepper.kt` — `GlassStepper`
- `ui/components/GlassToggle.kt` — `GlassToggle`

## Depends on (generated)
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.theme](ui-theme.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `LocalGlassStyle`, `LocalMotion`

## Used by (generated)
- [ui.apps](ui-apps.md) via `AppTile`, `FocusMemory`, `GlassButton`, `GlassChip`, `GlassDialog`, `GlassSearchField` +2
- [ui.home](ui-home.md) via `AppTile`, `FocusMemory`, `FrostSource`, `GlassButton`, `GlassCard`, `GlassIconButton` +4
- [ui.widgets](ui-widgets.md) via `GlassCard`, `GlassPill`

## Spec
- [design-system](../../spec/design-system.md), [focus-navigation](../../spec/focus-navigation.md)

<!-- kb:generated:end -->
