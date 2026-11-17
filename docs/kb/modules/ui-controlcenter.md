---
type: Module
title: ui.controlcenter
package: com.ks.tvlauncher.ui.controlcenter
spec: [../../spec/control-center.md, ../../spec/focus-navigation.md, ../../spec/inputs.md]
---

# ui.controlcenter

## Purpose
Control Center: the glass side panel (status tiles, Launcher rows), its compact Home card, `ControlCenterViewModel` (live Wi-Fi/Bluetooth/TV inputs), `InputsDialog` (switch to an HDMI input) and `SystemIntents` (Android settings pages, screensaver).

## Rules
- Opens with MENU (Home root key handler), holding OK on Home controls that have no other hold action (hero buttons, top-bar pill and gear, switch chip, bottom cards), or the Control Center card. Pinned tiles keep hold-OK for the app menu.
- Tiles only show status and open `Settings.ACTION_*` pages; an app cannot switch radios or power. Screensaver tries SystemUI's Somnambulator, else dream settings, never the main settings page; `SystemIntents.screensaverAvailable` hides the tile when neither resolves (both need the manifest `<queries>` entries).
- Tiles are a list laid out two per row (Wi-Fi, Bluetooth, Display, Inputs, Screensaver), so hidden tiles leave no gap. Focus starts on Wi-Fi, which is always shown.
- Inputs tile only when there are passthrough inputs. Its dialog is drawn inside the panel's focus group; OK starts `TvInputRepository.switchIntent` through `AppIntents.start`; failure shows a glass snackbar and keeps the list open; Back returns focus to the tile. Inputs uses `KsIcons.Plug`, never the monitor shape Display uses.
- The panel is modal (`modalFocus`). A dialog opened from it (Memory & storage) must be drawn through its `overlay` slot, inside the focus group, or focus cannot reach it.
- Back closes it; `HomeScreen` restores focus to the control focused before it opened, after the panel has gone.

<!-- kb:generated:start -->

## Files (generated)
- `ui/controlcenter/ControlCenterCard.kt` — `ControlCenterCard`
- `ui/controlcenter/ControlCenterPanel.kt` — `ControlCenter`, `ControlCenterActions`, `ControlCenterPanel`, `ControlCenterValues`
- `ui/controlcenter/ControlCenterViewModel.kt` — `ControlCenterViewModel`
- `ui/controlcenter/InputsDialog.kt` — `InputsDialog`
- `ui/controlcenter/SystemIntents.kt` — `SystemIntents`

## Depends on (generated)
- [data.system](data-system.md) via `BluetoothStatus`, `NetworkKind`, `NetworkStatus`, `SystemStatusRepository`, `TvInputRepository`
- [domain](domain.md) via `InputConnection`, `TvInput`, `WallpaperMode`, `Wallpapers`
- [root](root.md) via `KsApplication`
- [ui.apps](ui-apps.md) via `AppIntents`
- [ui.components](ui-components.md) via `GlassCard`, `GlassChip`, `GlassDialog`, `GlassSnackbar`, `glass`, `modalFocus` +1
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.theme](ui-theme.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `LocalMotion`

## Used by (generated)
- [ui.home](ui-home.md) via `ControlCenter`, `ControlCenterActions`, `ControlCenterCard`, `ControlCenterValues`, `ControlCenterViewModel`, `SystemIntents`
- [ui.settings](ui-settings.md) via `SystemIntents`

## Spec
- [control-center](../../spec/control-center.md), [focus-navigation](../../spec/focus-navigation.md), [inputs](../../spec/inputs.md)

<!-- kb:generated:end -->
