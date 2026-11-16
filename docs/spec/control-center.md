---
type: Screen
title: Control Center
stage: 4
mockups: [ControlCenter.png]
---

# Control Center

- Opens with MENU (if the remote has one), **long-press OK on Home**, or the Home card. Glass panel slides in from the right.
- Status tiles: Wi-Fi (SSID if permitted, else "Connected"/"Offline" via `ConnectivityManager`) · Bluetooth (on/off; device count only if permission already granted) · Display (resolution + refresh from `Display.mode`) · Screensaver (start the system dream if available).
- Each tile opens its `Settings.ACTION_*` page. Apps can't switch radios or power directly; the UI says so.
- Rows: Wallpaper, Clock format, Weather city, Subscriptions, Memory & storage (opens the [memory-storage-widget](memory-storage-widget.md) panel), Android settings.
- Back closes it and restores focus ([focus-navigation](focus-navigation.md)).
