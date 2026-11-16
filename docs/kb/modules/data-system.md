---
type: Module
title: data.system
package: com.ks.tvlauncher.data.system
spec: [../../spec/greeting-widget.md, ../../spec/memory-storage-widget.md]
---

# data.system

## Purpose
What the TV says about itself: TV name (live, via a content observer), tidied model and Android version. Stage 4 adds RAM and storage stats here.

## Rules
- No permissions and no polling; observers exist only while the flow is collected.

<!-- kb:generated:start -->

## Files (generated)
- `data/system/DeviceInfoRepository.kt` — `DeviceInfo`, `DeviceInfoRepository`

## Depends on (generated)
- [domain](domain.md) via `DeviceNames`

## Used by (generated)
- [root](root.md) via `DeviceInfoRepository`
- [ui.home](ui-home.md) via `DeviceInfo`, `DeviceInfoRepository`

## Spec
- [greeting-widget](../../spec/greeting-widget.md), [memory-storage-widget](../../spec/memory-storage-widget.md)

<!-- kb:generated:end -->
