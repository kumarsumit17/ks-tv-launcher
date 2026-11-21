---
type: Module
title: ui.components
package: com.ks.tvlauncher.ui.components
spec: [../../../specs/design-system.md, ../../../specs/focus-navigation.md]
---

# ui.components

## Purpose
Reusable glass building blocks and the single focus treatment (`animateDpadFocus`, `DpadFocusable`), plus focus helpers (`requestFocusSafely`, `modalFocus`) and the six code-drawn wallpapers (`BuiltInWallpaper`, used full screen on Home and as Settings swatches).

## Rules
- Every focusable control uses `animateDpadFocus`.
- `frost()` only on still surfaces, never inside scrolling rows or grids.
- A long-press action always has a second path.
- `GlassSearchField` opens the keyboard on OK only, never on focus. One Back ends editing: it is caught with `onPreInterceptKeyBeforeSoftKeyboard` before the keyboard takes it (Stage 5 TV fix). With `icon = null` and `ImeAction.Done` it is the plain text field used in dialogs.
- Overlays use `modalFocus()` and return focus to their opener.
- In lazy rows and grids use `FocusMemory` (a requester per item), not `Modifier.focusRestorer()`: on a real TV the restorer returned focus to the wrong item.
- `focusProperties` on a container does not affect moves that start from its children; set `up`/`down` on the focusable items themselves.
- `GlassDialog` takes focus back within a frame if it ends up outside the open panel (a focused control disappeared). A GlassDialog never opens another GlassDialog on top, or the two would fight over focus.

<!-- kb:generated:start -->

## Files (generated)
- `ui/components/AppTile.kt` — `AppTile`
- `ui/components/BuiltInWallpaper.kt` — `BuiltInWallpaper`, `builtInWallpaperName`
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
- [domain](domain.md) via `Wallpapers`
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.theme](ui-theme.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `LocalGlassStyle`, `LocalMotion`

## Used by (generated)
- [ui.apps](ui-apps.md) via `AppTile`, `FocusMemory`, `GlassButton`, `GlassChip`, `GlassDialog`, `GlassSearchField` +2
- [ui.controlcenter](ui-controlcenter.md) via `GlassCard`, `GlassChip`, `GlassDialog`, `GlassSnackbar`, `glass`, `modalFocus` +1
- [ui.home](ui-home.md) via `AppTile`, `BuiltInWallpaper`, `FocusMemory`, `FrostSource`, `GlassButton`, `GlassCard` +6
- [ui.settings](ui-settings.md) via `BuiltInWallpaper`, `DpadFocusable`, `GlassButton`, `GlassCard`, `GlassChip`, `GlassDialog` +8
- [ui.subscriptions](ui-subscriptions.md) via `FocusMemory`, `GlassButton`, `GlassCard`, `GlassChip`, `GlassDialog`, `GlassPill` +6
- [ui.widgets](ui-widgets.md) via `GlassButton`, `GlassCard`, `GlassDialog`, `GlassPill`, `PillTone`, `requestFocusSafely`

## Spec
- [design-system](../../../specs/design-system.md), [focus-navigation](../../../specs/focus-navigation.md)

<!-- kb:generated:end -->
