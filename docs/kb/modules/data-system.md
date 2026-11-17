---
type: Module
title: data.system
package: com.ks.tvlauncher.data.system
spec: [../../spec/greeting-widget.md, ../../spec/memory-storage-widget.md]
---

# data.system

## Purpose
What the TV says about itself: TV name (live, via a content observer), tidied model and Android version; `DeviceStatsRepository` (RAM and storage every 10 s, optional Apps/Other split); `SystemStatusRepository` (network and Bluetooth status for Control Center).

## Rules
- No runtime permissions are requested. Observers, callbacks and receivers exist only while a flow is collected.
- RAM/storage is the one allowed poll (10 s, spec), and only while Home collects it (`WhileSubscribed(0)`, lifecycle-aware collection, and only when the card is shown or the panel open).
- The Apps/Other split needs Usage access (`PACKAGE_USAGE_STATS`, granted by the user in Android settings) and is read only while the panel is open.
- SSID is shown only if Android reveals it (it hides it without location access); paired-device count only if Bluetooth permission is already granted.

<!-- kb:generated:start -->

## Files (generated)
- `data/system/DeviceInfoRepository.kt` — `DeviceInfo`, `DeviceInfoRepository`
- `data/system/DeviceStatsRepository.kt` — `DeviceStats`, `DeviceStatsRepository`, `StorageBreakdown`
- `data/system/SystemStatusRepository.kt` — `BluetoothStatus`, `NetworkKind`, `NetworkStatus`, `SystemStatusRepository`

## Depends on (generated)
- [domain](domain.md) via `DeviceNames`

## Used by (generated)
- [root](root.md) via `DeviceInfoRepository`, `DeviceStatsRepository`, `SystemStatusRepository`
- [ui.controlcenter](ui-controlcenter.md) via `BluetoothStatus`, `NetworkKind`, `NetworkStatus`, `SystemStatusRepository`
- [ui.home](ui-home.md) via `DeviceInfo`, `DeviceInfoRepository`, `DeviceStats`, `DeviceStatsRepository`
- [ui.settings](ui-settings.md) via `DeviceInfo`, `DeviceInfoRepository`
- [ui.widgets](ui-widgets.md) via `DeviceStats`

## Spec
- [greeting-widget](../../spec/greeting-widget.md), [memory-storage-widget](../../spec/memory-storage-widget.md)

<!-- kb:generated:end -->
