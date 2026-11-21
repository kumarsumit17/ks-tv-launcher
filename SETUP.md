# Setting up KS on your TV

KS - TV Launcher is a normal app. It never becomes, and never offers to become, your TV's Home launcher, so **keep your TV's current launcher as Home** and open KS from it. Details: [specs/app-mode.md](specs/app-mode.md).

## 1. Turn on ADB on the TV
1. Android settings › System › About › tap **Android TV OS build** seven times to unlock Developer options.
2. Android settings › System › Developer options › turn on **USB debugging** (on some TVs also **Network debugging** / **ADB debugging**).
3. Find the TV's IP address in Android settings › Network & Internet.

## 2. Install
From a computer on the same network, with the Android platform tools installed:

```bash
adb connect <tv-ip>:5555
adb install -r <apk>
```

Allow the debugging prompt on the TV the first time.

- **Debug build:** `app/build/outputs/apk/debug/app-debug.apk` (built with `./gradlew assembleDebug`).
- **Release build:** `app-release.apk` from the GitHub Releases page of this repository.

Debug and release builds are signed with different keys. To switch from one to the other, uninstall first (this deletes KS's data on the TV, including subscriptions, so export the CSV first if you need it):

```bash
adb uninstall com.ks.tvlauncher
```

Updating within the same kind of build (`adb install -r`) keeps all your data.

## 3. Open KS
- Open **KS TV** from your launcher's app list, or
- pin **KS TV** to your launcher's favourites row (in most launchers: highlight the app, hold OK, then choose *Add to favourites* or *Move*), or
- from a computer: `adb shell am start -n com.ks.tvlauncher/.MainActivity`.

Back on KS's main screen returns to your launcher and keeps KS ready for a fast reopen.

## 4. Optional permissions
- **Notifications** (renewal reminders): turn on *Remind me before renewals* in KS Settings › Renewal reminders. On Android 13 and newer KS asks for permission then. Android decides the exact delivery time, so a reminder can arrive later in the day.
- **Usage access** (Apps / Other split in Memory & storage): granted by you in Android settings from the Memory & storage panel. Off by default.
