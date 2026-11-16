---
type: Guide
title: TV setup
stage: 5
---

# TV setup (write as repo-root `SETUP.md`)

- KS is a normal app, not the Home launcher ([app-mode](app-mode.md)). Keep the TV's current launcher as Home.
- Install: `adb install -r <apk>` (debug: `app/build/outputs/apk/debug/app-debug.apk`; release: `app-release.apk` from GitHub Releases). Switching between debug and release needs `adb uninstall com.ks.tvlauncher` first.
- Open KS from the TV's app list, or pin "KS TV" to your launcher's favourites row.
- Do not document disabling the stock launcher or setting KS as Home.
