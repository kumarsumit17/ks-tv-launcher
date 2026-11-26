---
type: Module
title: ui.widgets
package: com.ks.tvlauncher.ui.widgets
spec: [../../../specs/greeting-widget.md, ../../../specs/memory-storage-widget.md]
---

# ui.widgets

## Purpose
Home widgets: `GreetingWidget` (greeting, date, TV name and Android version, switch chip), `MemoryStorageCard` (bars), `MemoryStoragePanel` (ring gauges, Manage apps / Free up space, Usage access offer) and the shared `UsageBar` / `UsageRing` drawing.

## Rules
- Widgets are stateless; data arrives through `HomeUiState` or as `DeviceStats`.
- Level colours and tags come only from `ByteFormatter.level` (blue < 75 %, amber 75–90 %, red > 90 %).
- The RAM button is "Manage apps" (`ACTION_APPLICATION_SETTINGS`); never "Running apps".

<!-- kb:generated:start -->

## Files (generated)
- `ui/widgets/GreetingWidget.kt` — `GreetingWidget`
- `ui/widgets/MemoryStorageCard.kt` — `MemoryStorageCard`
- `ui/widgets/MemoryStoragePanel.kt` — `MemoryStoragePanel`
- `ui/widgets/UsageVisuals.kt` — `UsageBar`, `UsageRing`, `usageColors`, `usageTag`, `usageTone`

## Depends on (generated)
- [data.system](data-system.md) via `DeviceStats`
- [domain](domain.md) via `ByteFormatter`, `GreetingPeriod`, `UsageLevel`
- [ui.components](ui-components.md) via `GlassButton`, `GlassCard`, `GlassDialog`, `GlassPill`, `PillTone`, `requestFocusSafely`
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.theme](ui-theme.md) via `KsColors`, `KsShapes`, `KsType`, `forText`

## Used by (generated)
- [ui.home](ui-home.md) via `GreetingWidget`, `MemoryStorageCard`, `MemoryStoragePanel`

## Spec
- [greeting-widget](../../../specs/greeting-widget.md), [memory-storage-widget](../../../specs/memory-storage-widget.md)

<!-- kb:generated:end -->
