---
type: Module
title: ui.widgets
package: com.ks.tvlauncher.ui.widgets
spec: [../../spec/greeting-widget.md, ../../spec/memory-storage-widget.md]
---

# ui.widgets

## Purpose
Home widgets. Now: `GreetingWidget` (greeting with name gradient, date, TV name and Android version, switch chip). Stage 4 adds the memory and storage card.

## Rules
- Widgets are stateless; data arrives through `HomeUiState`.

<!-- kb:generated:start -->

## Files (generated)
- `ui/widgets/GreetingWidget.kt` — `GreetingWidget`

## Depends on (generated)
- [domain](domain.md) via `GreetingPeriod`
- [ui.components](ui-components.md) via `GlassCard`, `GlassPill`
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.theme](ui-theme.md) via `KsColors`, `KsShapes`, `KsType`

## Used by (generated)
- [ui.home](ui-home.md) via `GreetingWidget`

## Spec
- [greeting-widget](../../spec/greeting-widget.md), [memory-storage-widget](../../spec/memory-storage-widget.md)

<!-- kb:generated:end -->
