---
type: Module
title: ui.theme
package: com.ks.tvlauncher.ui.theme
spec: [../../spec/design-system.md, ../../spec/performance.md]
---

# ui.theme

## Purpose
Design tokens and theme: colours, Inter type scale, shapes and dimens, motion policy, glass style (blur radius per intensity; no blur below API 31).

## Rules
- Values come from the design-system spec.
- With motion off, animation specs snap.

<!-- kb:generated:start -->

## Files (generated)
- `ui/theme/Color.kt` — `KsColors`
- `ui/theme/Motion.kt` — `KsMotion`, `LocalMotion`, `MotionPolicy`, `systemAnimationsEnabled`
- `ui/theme/Shape.kt` — `KsDimens`, `KsShapes`
- `ui/theme/Theme.kt` — `GlassStyle`, `KsTheme`, `LocalGlassStyle`, `VisualSettings`
- `ui/theme/Type.kt` — `InterFamily`, `KsType`, `KsTypography`

## Depends on (generated)
- [data.settings](data-settings.md) via `GlassIntensity`

## Used by (generated)
- [root](root.md) via `KsTheme`
- [ui.apps](ui-apps.md) via `KsColors`, `KsDimens`, `KsType`
- [ui.components](ui-components.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `LocalGlassStyle`, `LocalMotion`
- [ui.home](ui-home.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `LocalGlassStyle`, `LocalMotion` +1
- [ui.widgets](ui-widgets.md) via `KsColors`, `KsShapes`, `KsType`

## Spec
- [design-system](../../spec/design-system.md), [performance](../../spec/performance.md)

<!-- kb:generated:end -->
