---
type: Guide
title: Google TV setup
stage: 5
---

# Google TV setup (write as repo-root `SETUP.md`)

- Install: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
- Make KS the Home app. Many Google TV builds don't offer a Home-app choice, so document disabling the stock launcher: `adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx`, and undoing it with `adb shell pm enable com.google.android.apps.tv.launcherx`.
- Alternative: remap the Home button with a key-mapper app.
- Warn that this is at the user's own risk and may change after system updates. Android TV (non-Google TV) shows a normal Home chooser.
