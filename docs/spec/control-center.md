---
type: Screen
title: Control Center
stage: 4
mockups: [ControlCenter.png]
---

# Control Center

- Opens with MENU (if the remote has one), **long-press OK on Home** (on controls without their own hold action; pinned tiles keep it for the app menu), or the Home card. Glass panel slides in from the right.
- Status tiles: Wi-Fi (SSID if permitted, else "Connected"/"Offline" via `ConnectivityManager`) · Bluetooth (on/off; device count only if permission already granted) · Display (resolution + refresh from `Display.mode`) · Screensaver (start the system dream if available).
- Each tile opens its `Settings.ACTION_*` page. Apps can't switch radios or power directly; the UI says so.
- **Screensaver** starts the system dream (SystemUI's `Somnambulator`), else opens the screensaver settings page (`Settings.ACTION_DREAM_SETTINGS`). If the TV has neither (no dream component and no dream settings page), the tile is **hidden**; it never falls back to the main Android settings page. Checked each time the panel opens.
- Stage 6: an "Inputs" tile for HDMI sources ([inputs](inputs.md)), hidden when the TV has no passthrough inputs.
- Tiles sit two per row in this order: Wi-Fi, Bluetooth, Display, Inputs, Screensaver. A hidden tile leaves no gap; an odd last tile keeps half width.
- Rows: Wallpaper (cycles Dynamic → Custom → Plain), Clock format, Weather city, Subscriptions, Memory & storage (opens the [memory-storage-widget](memory-storage-widget.md) panel), Android settings.
- Back closes it and restores focus ([focus-navigation](focus-navigation.md)).
