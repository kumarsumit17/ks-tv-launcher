---
type: Module
title: ui.theme
package: com.ks.tvlauncher.ui.theme
spec: [../../../specs/design-system.md, ../../../specs/performance.md]
---

# ui.theme

## Purpose
Design tokens and theme: colours, Inter type scale, shapes and dimens, motion policy, glass style (blur radius per intensity; no blur below API 31).

## Rules
- Values come from the design-system spec.
- With motion off, animation specs snap.
- Every tween duration goes through `AnimationSpeed.scale` with the "Transition speed" percent (default 50).
- `LocalMotion` is a `compositionLocalOf`, never static: a static local recomposed the whole screen on every Reduce motion or speed change and froze the TV.

<!-- kb:generated:start -->

## Files (generated)
- `ui/theme/Color.kt` — `KsColors`
- `ui/theme/Motion.kt` — `KsMotion`, `LocalMotion`, `MotionPolicy`, `systemAnimationsEnabled`
- `ui/theme/Shape.kt` — `KsDimens`, `KsShapes`
- `ui/theme/Theme.kt` — `GlassStyle`, `KsCanvas`, `KsTheme`, `LocalGlassStyle`, `VisualSettings`
- `ui/theme/Type.kt` — `InterFamily`, `KsType`, `KsTypography`, `forText`

Tests: `ui/theme/KsCanvasTest.kt`

## Depends on (generated)
- [data.settings](data-settings.md) via `GlassIntensity`
- [domain](domain.md) via `AnimationSpeed`

## Used by (generated)
- [root](root.md) via `KsTheme`
- [ui.apps](ui-apps.md) via `KsColors`, `KsDimens`, `KsType`
- [ui.components](ui-components.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `LocalGlassStyle`, `LocalMotion`
- [ui.controlcenter](ui-controlcenter.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `LocalMotion`
- [ui.home](ui-home.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `LocalGlassStyle`, `LocalMotion` +2
- [ui.settings](ui-settings.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `forText`
- [ui.subscriptions](ui-subscriptions.md) via `KsColors`, `KsDimens`, `KsType`, `forText`
- [ui.widgets](ui-widgets.md) via `KsColors`, `KsShapes`, `KsType`, `forText`

## Spec
- [design-system](../../../specs/design-system.md), [performance](../../../specs/performance.md)

<!-- kb:generated:end -->
