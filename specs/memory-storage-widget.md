---
type: Specification
title: Memory and storage widget
description: "RAM and storage, used / total."
tags: [memory, storage, widget]
stage: 4
mockups: [MemoryStorage.png]
---

# Memory and storage widget

## Data (`data/system/DeviceStatsRepository`, no permissions)
- RAM: `ActivityManager.getMemoryInfo()` → `totalMem`, `availMem`; used = total − available.
- Storage: `StatFs(Environment.getDataDirectory().path)` → `totalBytes`, `availableBytes`.
- `Flow<DeviceStats>` polling every 10 s, only while Home is STARTED (`repeatOnLifecycle`); reads on `Dispatchers.IO`.

## Display
- Decimal GB, one decimal ("1.3 / 2.0 GB"), matching Android Settings; MB below 1 GB.
- Percent used: <75 % blue `#7FB2FF`→`#B9D3FF`; 75–90 % amber `#FFB45A`→`#FFD9A3`; >90 % red `#FF8A8A`. Tags: "Normal", "Getting full", "Almost full".
- **Home card**: two rows (label, bar, used / total), `animateDpadFocus`; OK opens the panel.
- **Panel** (`GlassDialog`, also from [control-center](control-center.md)): two ring gauges with percent; beside each, "X GB used of Y GB", free space, tag, one button:
  - RAM: "Manage apps" → `Settings.ACTION_APPLICATION_SETTINGS` (apps can't list other running apps; never label it "Running apps").
  - Storage: "Free up space" → `Settings.ACTION_INTERNAL_STORAGE_SETTINGS`.
  - Back closes and returns focus to the card.
- Optional Apps/Other breakdown only if the user grants Usage access (`PACKAGE_USAGE_STATS` via `Settings.ACTION_USAGE_ACCESS_SETTINGS`) using `StorageStatsManager`; off by default, offered from the panel with a one-line explanation.
- The card can be hidden in [settings](settings.md).
