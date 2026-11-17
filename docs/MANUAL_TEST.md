# Manual test checklist

On-device checks that unit tests cannot cover. Use a real TV or the Android TV emulator (1080p); phone emulators hide focus bugs. Install with `./gradlew installDebug`, then open "KS TV" from the TV's app list.

## Stage 1: Foundation

Checks that Stage 2 replaced (the six empty slots, Back doing nothing, the Home-key reset and Home-app registration) have been removed; their successors are under Stage 2.

### Main screen
- [ ] Cold start shows a dark navy screen at once, with no white flash.
- [ ] Top bar shows the "KS" badge, "TV Launcher", the time, the short date, a dimmed "Weather not set up" pill and the gear.
- [ ] The clock changes at the minute boundary, within a second or two.
- [ ] Changing the TV's time, time zone or 12/24-hour setting updates the clock without restarting the app.
- [ ] The greeting matches the time of day: morning 5:00–11:59, afternoon 12:00–16:59, evening 17:00–4:59.
- [ ] With no name saved, the greeting has no name after it.
- [ ] The second line is the long date in the TV's language.
- [ ] The third line shows the TV's name from Android Settings › Device name and the correct Android version.
- [ ] Renaming the TV in Android settings updates the third line when you return to KS.
- [ ] Text is fully inside the screen edges (no overscan clipping).
- [ ] The weather pill is skipped by the D-pad.
- [ ] OK on the gear opens Android settings; Back returns to KS with focus on the gear.

### Motion and effects
- [ ] With Developer options › Animator duration scale set to "Animation off", focus changes are instant.
- [ ] On an API 31+ device the weather pill looks frosted; on older devices it is plain translucent glass.

## Stage 2: Apps

### App mode
- [ ] The TV's own launcher lists the app as "KS TV", with the "KS" banner and no clipped text.
- [ ] `adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME` still prints the TV's own launcher, and Android never shows a Home-app chooser because of KS.
- [ ] Back on the main screen with nothing open returns to the TV's launcher.
- [ ] Opening KS again shows the same screen at once (it was kept in the background), with focus on the tile that was focused before.
- [ ] Opening KS again while the drawer or a menu was open shows the main screen with them closed.
- [ ] The Home key goes to the TV's launcher, as for any app.
- [ ] Optional, with a device attached: `./gradlew connectedDebugAndroidTest` passes (`HomeIntentTest`).

### Pinned row and hero
- [ ] First run: the installed ones among Netflix, Prime Video, YouTube, JioHotstar, Spotify, Plex and Kodi are pinned, in that order.
- [ ] With none of those installed, the hero reads "No pinned apps yet" and focus starts on the All apps card.
- [ ] Each tile shows the app's own banner; an app without a TV banner shows its icon centred on glass.
- [ ] On launch, focus is on the first pinned tile. Only the focused tile shows its label.
- [ ] Left and Right move along the row; it scrolls so the focused tile, its border and glow are fully visible at both ends.
- [ ] The hero title changes to the focused app.
- [ ] OK on a tile opens that app. Returning to KS puts focus back on that tile.
- [ ] Up from any tile goes to "Open ‹app›"; Down from the hero buttons returns to the same tile.
- [ ] Down from any tile goes to the All apps card; Up from the card returns to the same tile.
- [ ] "Open ‹app›" opens the app. "Unpin" removes it from the row and the hero moves to another app.
- [ ] Unpinning the last app shows the empty hero and moves focus to the All apps card.
- [ ] Holding OK on a tile opens the menu; Back closes it and focus returns to the tile.

### All apps drawer
- [ ] OK on the All apps card opens the drawer; the card shows the right installed count.
- [ ] Focus starts in the grid. The D-pad cannot reach anything behind the drawer.
- [ ] The grid has six columns and lists every launchable app except KS, A to Z, with PINNED after pinned apps.
- [ ] Moving past the search field with the D-pad does not open the keyboard. OK on it does; typing filters the grid.
- [ ] The keyboard's Search key, or Back, closes the keyboard and focus stays on the search field.
- [ ] Only categories that have apps are shown. Choosing one filters the grid; All shows everything.
- [ ] The A–Z chip switches to Z–A and reverses the order.
- [ ] A search with no result shows "No apps match".
- [ ] OK on a tile opens the app.
- [ ] Holding OK opens the menu. With Edit on, OK opens the same menu.
- [ ] Menu › Pin to Home adds the app to the end of the pinned row; Unpin removes it.
- [ ] Menu › Move left / Move right changes its place in the row; the menu stays open and the buttons disappear at the ends.
- [ ] Menu › App info opens the system page for that app.
- [ ] Menu › Uninstall opens the system uninstall dialog; nothing is removed without confirming there.
- [ ] Back closes the menu first, then the drawer. Focus returns to the All apps card.
- [ ] The next time the drawer opens, the search is empty and Edit is off.

### Apps changing underneath
- [ ] Installing an app while KS is open adds it to the drawer and updates the count without a restart.
- [ ] Uninstalling a pinned app removes its tile without a restart, and it stays gone after reinstalling.
- [ ] Uninstalling an app from another device (`adb uninstall`) while its tile is focused, then pressing OK on a stale tile, shows "‹app› is no longer installed" and the list refreshes.
- [ ] Updating an app that changed its banner shows the new banner.

### Dynamic wallpaper
- [ ] Resting on a tile for a moment changes the background to a soft, tinted version of that app's banner.
- [ ] Moving quickly along the row does not flicker through every app; only the tile you stop on loads.
- [ ] The change is a crossfade, and text stays readable over bright artwork.
- [ ] With animations off (animator scale 0), the wallpaper changes without a fade.

### Persistence
- [ ] After force-stopping KS or rebooting the TV, the pinned apps and their order are unchanged.
- [ ] After a reboot the TV still starts on its own launcher, not KS.

_Not yet testable: the profile switch chip (needs two saved names; the profile dialog arrives in Stage 4), turning the dynamic wallpaper off (its switch arrives with Settings in Stage 4), and `GlassToggle` and `GlassStepper` (first used on screen in later stages)._

## Stage 3: Subscriptions

### Open and close
- [ ] The bottom strip shows the Subscriptions card next to All apps; Up from either goes to the last focused pinned tile.
- [ ] OK on the card opens Active Subscriptions with focus on Add subscription. Back (or the Home button) returns to Home with focus on the card.
- [ ] With the screen open, reopening KS from the TV's launcher closes it and focuses the last pinned tile.

### Add, edit, delete
- [ ] First run shows the empty message, zero stat cards and a disabled Export CSV.
- [ ] Add subscription: every control is reachable with the D-pad; OK on Name opens the keyboard, Done/Back closes it.
- [ ] Cost: OK on − / + steps ₹10; holding OK steps ₹100; it never goes below ₹0.
- [ ] Monthly/Yearly chips, « ‹ date › » (month/day) and the Active switch all respond to OK. Save stays disabled until a name is entered.
- [ ] After Save, focus lands on the new row; stat cards and the Home card update at once.
- [ ] OK on a row opens it filled in; Save keeps focus on the row; Delete removes it and focus goes to Add subscription; Cancel and Back change nothing.

### Toggle, totals, renewals
- [ ] Right from a row reaches its Active/Paused pill; OK toggles it, and the totals and active count update.
- [ ] A yearly ₹1,499 shows "≈ ₹125 / month"; a monthly ₹499 shows "₹5,988 / year"; large totals use Indian grouping (₹1,23,456).
- [ ] A renewal date in the past shows the next date after rolling forward; Next renewal names the soonest active one ("in N days · date").

### Export and persistence
- [ ] Export CSV opens the share sheet (or shows a notice if no app can receive text/csv). The CSV has the header `name,amount_minor,currency,cycle,next_due`.
- [ ] After force-stopping KS or rebooting, every subscription, its status and order are unchanged.

## Stage 4: Control Center, Settings, weather, profile, memory & storage

Use sample data whose names start with `TEST ` and remove it afterwards ([verification](spec/verification.md)).

### First run and profile
- [ ] On a TV with no saved names, "Who's watching?" opens by itself once Home has loaded. Cancel closes it and it does not come back after reopening KS.
- [ ] Settings › Profile › Who's watching: OK on the name field opens the keyboard; type `TEST Goldi`, Done, then Add name: the chip appears selected. Add `TEST Sumit`.
- [ ] OK on a chip selects it. Holding OK on a chip, or "Rename or remove ‹name›", opens Rename/Remove; Rename to `TEST Gold` keeps its place; Remove drops it; Back returns without changes.
- [ ] TV name, Model and Android version (with API level) show AUTO values. "Use my own TV name" reveals a field; with a name typed and Save, the greeting's line 3 shows it.
- [ ] Save updates the greeting and the "Switch to …" chip at once; Cancel and Back change nothing. A 9th name cannot be added.
- [ ] Focus returns to the Profile row after the dialog closes.

### Weather
- [ ] On a TV with no city set, the pill shows a shimmer, then the detected city (e.g. "19° Clear · Bengaluru") without any setup.
- [ ] OK on the pill opens Settings at Weather with focus on Detect again; the line reads "‹city, region, country› · automatic" with the privacy line under it.
- [ ] Detect again refreshes the city; with the network off it shows "Could not detect the city. The last one is kept." and the pill keeps the old city.
- [ ] Change city reveals the search field; search a city (e.g. Pune): up to five places; choosing one shows "· manual", focus moves to "Use my current city (automatic)", and the pill changes.
- [ ] "Use my current city (automatic)" goes back to the detected city.
- [ ] °C/°F switches the pill at once. Back returns focus to the pill.
- [ ] With the network off, reopening KS shows the cached reading dimmed; with no cache it shows "Weather unavailable"; with no city ever detected it shows "Set city".
- [ ] Control Center's Weather city row shows "‹city› · automatic · °C" (or the manual city).

### Memory & storage
- [ ] The card shows RAM and Storage bars with "used / total" in decimal GB (MB below 1 GB), matching Android Settings › Storage within rounding.
- [ ] Colours: blue under 75 %, amber 75–90 %, red over 90 %.
- [ ] OK opens the panel: two ring gauges, used/free lines and tags. Manage apps opens Android's app settings; Free up space opens storage settings; Back returns focus to the card.
- [ ] "Show Apps / Other" opens Usage access (or Android settings). After allowing KS TV, the storage line adds Apps and Other.
- [ ] Settings › Show Memory & storage card off hides the card; the strip still navigates left/right and Up goes to the pinned row.

### Control Center
- [ ] Opens from the Control Center card, from MENU (if the remote has one), and by holding OK on the hero buttons, weather pill, gear, switch chip or a bottom card. Holding OK on a pinned tile still opens the app menu.
- [ ] Panel slides in from the right (no slide with Reduce motion). Focus starts on Wi-Fi; Up/Down/Left/Right reach every tile and row; focus never leaves the panel.
- [ ] Wi-Fi shows "Connected"/"Offline" (or the SSID if Android allows it), Ethernet shows "Ethernet"; Bluetooth shows On/Off/Not available; Display shows resolution and refresh rate.
- [ ] Each tile opens its Android settings page; Screensaver starts the screensaver or opens its settings. On a TV with neither (no Somnambulator, no dream settings page) the Screensaver tile is not shown.
- [ ] Wallpaper toggles Dynamic/Plain; 12-hour/24-hour changes the top-bar clock at once.
- [ ] Weather city opens Settings at Weather; Subscriptions opens the Subscriptions screen; Memory & storage opens the panel over Control Center and Back returns to that row.
- [ ] Back (or MENU) closes Control Center and focus returns to the control that was focused before.
- [ ] Reopening KS from the TV's launcher with Control Center, Settings or the panel open closes it and focuses the last pinned tile.

### Settings
- [ ] Every row is reachable with the D-pad; Back or Home returns to Home with focus on the gear.
- [ ] Dynamic wallpaper, glass intensity (Low = no blur), Reduce motion, clock Follow TV/12/24, Show date, currency (Subscriptions totals change symbol), pinned order (Move left/right reflects on Home at once).
- [ ] About shows the version name and code.

### Persistence
- [ ] After force-stopping KS or rebooting: names, active name, own TV name, city, unit, card visibility and every switch are unchanged.

## Stage 5: Polish (reminders, loading state, text fields, setup)

### Loading and empty states
- [ ] Cold start (`adb shell am force-stop com.ks.tvlauncher`, then open KS): until the app scan finishes the hero reads "Loading your apps…" and the All apps card "Loading…", never "No pinned apps yet · 0 installed". The Subscriptions card reads "Loading…", never "0 · Track what you pay for", until the list has been read.
- [ ] With every app unpinned, the hero reads "No pinned apps yet" and the card shows the real installed count.
- [ ] Subscriptions with no entries shows its empty line; weather offline states are unchanged from Stage 4.

### Text fields
- [ ] In the All apps search, Settings › Weather city search, the profile name field and the subscription dialog fields: OK opens the keyboard; **one** Back hides it and leaves editing, with focus on the same field. A second Back then closes the screen or dialog as usual.
- [ ] The keyboard's Search / Done key also ends editing.

### Renewal reminders
- [ ] Settings › Renewal reminders: the toggle is off by default; the lead-time stepper is hidden while off.
- [ ] On Android 13+: turning it on shows Android's notification permission prompt. Allow: the toggle turns on and "3 days before" appears. Deny: the toggle stays off and the "Notifications are off for KS" line and "Open notification settings" row appear; the row opens KS's notification (or app info) page. After allowing notifications there and pressing Back, the line and row disappear and focus lands on the toggle.
- [ ] The stepper goes from 1 to 14 days; − at 1 and + at 14 do nothing and keep focus.
- [ ] With reminders on, add "TEST Netflix", ₹499 monthly, renewing tomorrow: a notification "TEST Netflix renews tomorrow" (₹499.00 · Monthly · date) arrives within a minute or so. Selecting it opens KS.
- [ ] Add "TEST Prime", ₹1,499 yearly, renewing in 10 days with a 3-day lead: no notification. Raise the lead to 10 days: it arrives.
- [ ] Pausing or deleting TEST Netflix removes its notification. Moving its date outside the window removes it; moving it inside again re-notifies.
- [ ] Each renewal is notified once: reopening KS or the next daily check does not repeat it.
- [ ] Turning reminders off removes all KS notifications.
- [ ] After a reboot with reminders on, the daily job is still registered (`adb shell dumpsys jobscheduler | grep -c "JOB androidx.work.systemjobscheduler:u0a<KS uid>"` is 1; the uid is in `adb shell dumpsys package com.ks.tvlauncher | grep userId`).
- [ ] Remove every `TEST ` subscription afterwards.

### Performance pass
- [ ] `adb shell am start -W -n com.ks.tvlauncher/.MainActivity` after force-stop: record TotalTime (cold start).
- [ ] `adb shell dumpsys meminfo com.ks.tvlauncher`: record TOTAL PSS on Home after scrolling the pinned row and opening/closing the drawer.
- [ ] `adb shell top -n 1 | grep tvlauncher` on an idle Home screen: CPU near 0 %.
- [ ] With reminders off no KS job is registered (the `grep -c "JOB androidx.work.systemjobscheduler:u0a<KS uid>"` count above is 0). Lines under the job history are past runs, not registered jobs.
- [ ] Measure cold start on a release build (or after `adb shell cmd package compile -m speed -f com.ks.tvlauncher` on debug): debug builds run un-optimised and are about 2.5x slower.

### Setup
- [ ] Following [SETUP.md](../SETUP.md) from a clean TV installs KS, and "KS TV" can be pinned to the launcher's favourites. Home stays the TV's own launcher.

## Stage 6: Release checklist

### KS TV Dev (test copy)
- [ ] `./gradlew installDebug` installs `com.ks.tvlauncher.dev` next to the real KS TV; `adb shell pm list packages | grep tvlauncher` shows both, and the real app's `versionName` and data are unchanged.
- [ ] The dev tile reads "KS TV Dev" with an amber banner; the real tile still reads "KS TV" in navy.
- [ ] `adb shell am start -n com.ks.tvlauncher.dev/com.ks.tvlauncher.MainActivity` opens KS TV Dev empty (its own data, not the owner's).
- [ ] KS TV Dev lists the real "KS TV" in All apps and hides only itself; the real KS TV lists "KS TV Dev" and hides only itself.
- [ ] Reminders in KS TV Dev: a `TEST ` subscription notifies as "KS - TV Launcher Dev", and turning them off there leaves the real app's job and notifications alone.
- [ ] `app-release.apk` from the Release workflow is labelled "KS TV", package `com.ks.tvlauncher` (`aapt2 dump badging`), with no ".dev" anywhere.

### Full on-device check (release build, after the one-time switch)
Record each run here: date, build version, pass or the fault found.
- [ ] Memory card on/off, glass intensity (Low/Medium/High), Reduce motion, currency, pinned order, Screensaver, weather with Wi-Fi off.
- [ ] Every earlier stage's checks above, run against `com.ks.tvlauncher`.

#### 2026-11-17 · v0.5.0 (500), release, KTC Lumio Vision (Android 14, API 34)
Run by Claude Code over adb. Every setting was noted first, changed alone and restored before the next.
- Pass · cold start after force-stop: 616, 606, 593 ms (608 ms later in the run). Debug was about 6.3 s.
- Pass · memory and CPU: 57.8 MB PSS on Home (debug 75 MB); 0 % CPU on idle Home.
- Pass · Memory card (On): Off hides the card and the Subscriptions card widens; On brings it back. Focus returns to the gear.
- Pass · glass intensity (Medium): over the app menu, Low is clear (tiles show through sharply), High is frosted. Restored to Medium. The pill over the flat wallpaper looks the same at every level, as expected. Note: entering the row focuses Medium even when Low is selected.
- Pass · Reduce motion (Off): the same four pinned-row moves rendered 28 frames with it on and 111 with it off (`dumpsys gfxinfo`). Restored to Off.
- Pass · currency (INR): CAD shows CA$0 on the Subscriptions screen. Restored to INR.
- Pass · pinned order: moving Castle for TV left put it before YouTube on Home; moved back to the original order (Netflix, Prime Video, JioHotstar, YouTube, Castle for TV, Jellyfin).
- **Fault** · Screensaver: "Start now" opened the main Android Settings. This TV has no `com.android.systemui/.Somnambulator`, no `android.settings.DREAM_SETTINGS` page and no screensaver set (`screensaver_components` is null), so both routes fail and the code falls back to the main settings page. The tile still says "Start now". Control Center keeps focus on the tile on return.
- Pass · subscriptions save and reload: "TEST Netflix", ₹500 monthly, due today, still there after `am force-stop` and a cold start; then deleted.
- Pass · reminders (Off): On, with no prompt (`POST_NOTIFICATIONS` granted); "TEST Netflix renews today · ₹500 · Monthly · 17 Nov 2026" was posted and one daily job registered (`JOB androidx.work.systemjobscheduler:u0a152/0`). Off removed the notification and the job. Restored to Off.
- Pass · weather: the automatic city (Bengaluru, °C) loads on the release build, and the pill updated from 21° to 22° during the run.
- Pass · weather with Wi-Fi off (run by the owner): the pill kept the last reading, dimmed, and the rest of the app worked normally. With Wi-Fi back on, the weather refreshed within about 5 minutes.
- Not run · every earlier stage's full checklist on the release build: only the items above were run.
- Owner data unchanged: names, subscriptions (empty) and pins left as they were. The `TEST ` item is removed.

## Stage 6a: Inputs (test in KS TV Dev)
- [ ] Control Center shows an Inputs tile ("3 inputs" or similar) next to Display; tiles sit two per row with no gap where a tile is hidden.
- [ ] OK on Inputs opens a glass list: HDMI inputs first in port order, then AV/component; each row shows Connected, Standby or Not connected. A name set in TV settings replaces "HDMI n" but keeps its place.
- [ ] Up/Down move through the list; focus never leaves it. Back closes it and focus returns to the Inputs tile; Back again closes Control Center.
- [ ] OK on an input switches the TV to it; Home/Back from the input player returns to the TV's launcher, and reopening KS TV Dev works normally.
- [ ] Plugging or unplugging an HDMI device while the list is open updates its state without reopening.
- [ ] If the switch cannot start, a glass snackbar says so and the list stays open.
- [ ] A TV with no passthrough inputs shows no Inputs tile.
- [ ] Screensaver tile: hidden on a TV with no Somnambulator and no dream settings page (Lumio Vision); where either exists it shows and starts the screensaver or opens its settings, never the main Android settings.

#### 2026-11-17 · KS TV Dev (debug, uncommitted Stage 6a), KTC Lumio Vision (Android 14, API 34)
Run by Claude Code over adb. The real KS TV (0.5.0) was not touched. Inputs has no data entry, so no `TEST ` items were needed.
- Pass · Control Center shows Wi-Fi, Bluetooth / Display, Inputs ("4 inputs"); no Screensaver tile and no gap.
- Pass · the list shows HDMI 1, HDMI 2, HDMI 3 (Standby) and Composite (Not connected), in that order. Down past the last row and Left/Right keep focus in the list; Back closes it and focus returns to the Inputs tile.
- Pass · OK on HDMI 2 opened Google's input player, and the active TV input session was `HdmiInputService/HW3` (HDMI 2). Home, then reopening KS TV Dev, closed the overlays and focused the first pinned tile.
- Pass · after Cowork review: the Inputs tile and each list row use a plug icon, clearly unlike Display's monitor.
- Not run · switch-failure snackbar, a TV with no passthrough inputs, plugging or unplugging while the list is open, and a TV that does have a screensaver.

## Stage 6b: Wallpaper (test in KS TV Dev)
- [ ] Settings › Wallpaper: Background chips Dynamic / Custom / Plain; Dynamic follows the focused app as before, Plain shows the navy background.
- [ ] Six built-in swatches (Midnight, Aurora, Dusk, Ember, Ocean, Mist): OK on one selects Custom, shows a check, and Home shows it full screen behind readable text.
- [ ] Dim (Custom only): Low / Medium / High visibly darken the wallpaper; switching Dim does not flash or reload.
- [ ] Control Center › Wallpaper cycles Dynamic → Custom → Plain and Home follows each time.
- [ ] Choices survive `am force-stop` and a cold start.
- [ ] Updating from a build with "Dynamic wallpaper" off opens in Plain (migration).
- [ ] "Choose a picture" appears only on a TV with a real document picker (not on the Lumio Vision, whose only handler is the framework stub). Where it appears: a picture from USB shows on Home, a large photo is stored at most 1920×1080 (`adb shell run-as com.ks.tvlauncher.dev ls -l files`), "Your picture" swatch appears, "Remove image" deletes it and focus stays in Settings.
- [ ] Missing picture (`run-as … rm files/wallpaper.jpg` while Custom shows it, then reopen KS): Home shows Plain and Settings says the picture couldn't be read; a new choice clears the note.
- [ ] D-pad: every swatch, chip, row and button is reachable; focus never leaves Settings.

#### 2026-11-17 · KS TV Dev (debug, uncommitted Stage 6b), KTC Lumio Vision (Android 14, API 34)
Run by Claude Code over adb. The real KS TV (0.5.0) was not touched.
- Pass · migration: the 6a build of KS TV Dev with the old switch set to Plain, then 6b installed over it, opened in Plain.
- Pass · Control Center › Wallpaper cycled Plain → Dynamic (Netflix's red tint) → Custom (Midnight), and Home followed each time.
- Pass · Settings › Wallpaper: mode chips, six swatches and Dim (Custom only). Selecting Ember showed a warm brown background behind readable text; Dim High measured about half the brightness of Low. Custom + Ember survived `am force-stop` and a cold start.
- Fixed during the test · the selected swatch's check badge was stretched across the whole swatch (`DpadFocusable` passes minimum size to its children); it is now a small corner badge.
- Pass · "Choose a picture" is hidden on this TV (its only `OPEN_DOCUMENT` handler is the framework stub).
- Pass · picture path, with a `TEST_wallpaper.jpg` copied into the dev app's `files/wallpaper.jpg` over `run-as`: the "Your picture" swatch and "Remove image" appeared; selecting it showed the picture full screen on Home with the dim.
- Pass · fallback: deleting the copy while it was showing, then a cold start, gave a Plain Home, and Settings showed Plain with "Your picture couldn't be read…". Choosing Ocean cleared the note.
- Pass · "Remove image" while the picture was showing deleted the file, set Plain, removed the swatch, and moved focus to the Dynamic chip; the D-pad kept working in Settings.
- Not run · importing through a real document picker (none on this TV), so the decode and downscale of a picked file were not exercised on a device; a TV with a file picker is needed for that.
- Test file removed from `/data/local/tmp`; only KS TV Dev data was used.

## Transition speed and Reduce motion (test in KS TV Dev)
- [ ] Settings › Look and motion › Transition speed shows 0.5× on a fresh install or after updating; − and + step through 0.25×, 0.5×, 0.75×, 1×, 1.25×, 1.5× and stop at the ends.
- [ ] At 0.5× focus moves, the Control Center slide and dialogs are visibly quicker than at 1×; 1.5× is slower. The value survives a force-stop.
- [ ] Reduce motion: every OK press flips the toggle, also when pressed quickly; with it on nothing animates and the hint under Transition speed says it has no effect.

#### 2026-11-17 · KS TV Dev (debug, uncommitted fix), KTC Lumio Vision (Android 14, API 34)
Run by Claude Code over adb. The real KS TV was not changed (only focus moved on its Home and Settings to record the owner's data for the release check).
- Measured before the fix · six pinned-row moves: KS TV Dev (debug, Reduce motion on) 13 frames, 100 % janky, median 105 ms; the real KS TV 0.5.0 (release) 110 frames, 65 % janky, median 69 ms. The TV renders slowly in both builds.
- Before the fix · one of six Reduce motion presses was lost (pressed 1.5 s after the previous one).
- Pass · Transition speed shows 0.5× after updating; + stops at 1.5×, − stops at 0.25×; 1× survived a force-stop; left at 0.5×.
- Pass · three Control Center open/close cycles: 55 frames at 0.5×, 72 at 1×.
- Pass · Reduce motion flipped on all 8 presses at 1 s gaps; with it on, the hint under Transition speed says it has no effect.
- Seen, not changed · Left from the leftmost control of a Settings row (for example − of Transition speed) moves focus to the Home button at the top-left; OK there closes Settings. A stray OK opened the profile dialog in KS TV Dev; it was cancelled, nothing saved. The dev wallpaper changed to Ember by a stray press (test data only).

#### 2026-11-17 · v0.6.1 (601), release, KTC Lumio Vision (Android 14, API 34): data check after the update
Release: v0.6.0 was never published (its run failed in the signing check script) and its tag was deleted. v0.6.1 (6a + 6b + the transition-speed fix) published by the Release workflow; APK and AAB signer SHA-256 `2aa25f08e6b8addc54f9af9b8ccabcd9cf890902b1939b5ca2e6ac25a3776448`, checked again on the downloaded APK (package `com.ks.tvlauncher`, 0.6.1 / 601, no ".dev"). Installed with `adb install -r` over 0.5.0; first-install time unchanged (08:06:08), so it was an update in place.

Baseline recorded on 0.5.0 at about 09:47, compared on 0.6.1 at about 10:47:
- Pass · profile names: Sumit (active).
- Pass · TV name: Living Room · Android 14.
- Pass · pinned apps and order: Netflix, Prime Video, JioHotstar, YouTube, Castle for TV, Jellyfin.
- Pass · subscriptions: none (as before); currency INR.
- Pass · weather: automatic, Bengaluru, Karnataka, India, °C.
- Pass · settings: Memory card on, glass Medium, Reduce motion off, clock Follow TV, show date on, reminders off.
- New · Transition speed 0.5× (the default, as intended).
- Pass · wallpaper: Dynamic on 0.6.1. The 0.5.0 baseline had "Dynamic wallpaper" off, but the owner confirmed turning it on in 0.5.0's Control Center after the baseline and before the update, so Dynamic is the correct migrated value. The off → Plain migration itself passed in KS TV Dev.
- Note · right after the install the TV showed a black screen and dropped off the network (standby); no KS crash was logged, and KS opened normally once the TV was back.
- KS TV Dev (`com.ks.tvlauncher.dev`) uninstalled after the check; the real app was never uninstalled or cleared.

#### 2026-11-17 · v0.7.0 (700), release, KTC Lumio Vision (Android 14, API 34): data check after the update
Release: v0.7.0 (6c part 1 smoothness + 6c part 2 in-app updates) tagged from `main` (`e6f199d`), published by the Release workflow (run 44036675156, success) with `app-release.apk` (1,937,327 bytes) and `app-release.aab`; APK and AAB signer SHA-256 `2aa25f08e6b8addc54f9af9b8ccabcd9cf890902b1939b5ca2e6ac25a3776448`, checked again on the downloaded APK with `apksigner` (package `com.ks.tvlauncher`, 0.7.0 / 700). Installed with `adb install -r` over 0.6.1; first-install time unchanged (08:06:08), so it was an update in place.

Baseline recorded on 0.6.1 at about 12:45 (fresh, from KS's own screens), compared on 0.7.0 at about 12:48:
- Pass · profile names: Sumit (active).
- Pass · TV name: Living Room · Android 14.
- Pass · pinned apps and order: Netflix, Prime Video, JioHotstar, YouTube, Castle for TV, Jellyfin.
- Pass · subscriptions: none; currency INR.
- Pass · weather: automatic, Bengaluru, Karnataka, India, °C.
- Pass · wallpaper: Dynamic.
- Pass · settings: Memory card on, glass Medium, Reduce motion off, Transition speed 0.5×, clock Follow TV, show date on, reminders off.
- New · About: "Version 0.7.0 (700)", "Check for updates automatically" on (default), "Check for updates". No "Update available" pill (no newer release).
- Fault · "Check for updates" cannot be reached with the D-pad when scrolling through Settings: Down from the automatic-check switch goes straight to "Android settings", and Up from "Android settings" goes back to the switch. The narrow left-aligned button loses Compose's focus search to the full-width rows above and below it (the search penalises the sideways offset between centres). It is reachable only when Settings opens at About (Home pill or notification), which focuses it directly. Needs a fix through [verification](spec/verification.md) before v0.7.1.
- KS TV Dev (`com.ks.tvlauncher.dev`) uninstalled after the check; the real app was never uninstalled or cleared.

## Stage 6c part 1: Smoothness (test in KS TV Dev, `perf` build)
How: `./gradlew assemblePerf`, `adb install -r app/build/outputs/apk/perf/app-perf.apk` (installs as KS TV Dev, never over the real app), then `scripts/perf.sh` (scenarios 1, 3, 4, 5) and `scripts/perf.sh 2` with the background set to Plain. Each run: force-stop, cold start, 5 s settle, `gfxinfo reset`, the keys, `gfxinfo`. Three runs per scenario; the medians are shown. Glass Medium, Transition speed 0.5×, Reduce motion off, 8 pinned apps, 21 installed.
- [ ] `scripts/perf.sh` scenario 1 on the `perf` build: at most 25 % janky and a median of 33 ms or less.
- [ ] No scenario worse than the baseline below.
- [ ] Home looks the same as before in Dynamic (red Netflix wash, readable text, frosted weather pill), a built-in wallpaper and Plain.

#### 2026-11-17 · KS TV Dev 0.6.1 (`perf` build: release code, debug key), KTC Lumio Vision (Android 14, API 34, armeabi-v7a, UI 1920×1080)
Run by Claude Code over adb. The real KS TV was not opened, changed or measured in this pass.

| Scenario | Before: frames | janky | p50 | p90 | p95 | After: frames | janky | p50 | p90 | p95 |
|---|---|---|---|---|---|---|---|---|---|---|
| 1 Pinned row, Dynamic | 217 | 66.4 % | 69 ms | 93 ms | 97 ms | 293 | 8.2 % | 32 ms | 40 ms | 42 ms |
| 2 Pinned row, Plain | 98 | 76.3 % | 77 ms | 89 ms | 93 ms | 129 | 11.6 % | 36 ms | 46 ms | 48 ms |
| 3 Control Center ×3 | 65 | 86.2 % | 85 ms | 125 ms | 150 ms | 82 | 22.0 % | 24 ms | 57 ms | 81 ms |
| 4 All apps drawer | 109 | 67.9 % | 61 ms | 85 ms | 89 ms | 126 | 13.5 % | 36 ms | 44 ms | 46 ms |
| 5 Settings scroll | 475 | 55.3 % | 57 ms | 73 ms | 81 ms | 564 | 16.0 % | 25 ms | 36 ms | 38 ms |

- Pass · target met: scenario 1 at 8.2 % janky, median 32 ms (target ≤ 25 %, ≤ 33 ms). More frames in the same time means fewer were dropped.
- Pass · every scenario better than its baseline.
- Pass · screenshots: Dynamic and the Ocean built-in look as before; the frosted weather pill still blurs.
- Note · Plain's p50 sits on a frame-time boundary: runs give 23–24 or 36 ms with the same janky %, p90 and p95.
- Not possible on this TV · a picked picture (Custom with your own image): the TV has no file picker. That path keeps full resolution and the same cached layer.
- Test data in KS TV Dev only: 4 extra pins (Apple TV, Castle for TV, Hoichoi, Jellyfin); wallpaper and glass changed for measuring and set back to Dynamic and Medium.

## Stage 6c part 2: In-app updates (KS TV Dev for the riders and the debug note; release builds for the update itself)

### In KS TV Dev (debug)
- [x] Settings › About: version line, then "This is a test build. Updates are off…", then Android settings. No update switch or buttons. (Superseded after v0.7.0: the rows now show as an inert preview; see the fix section below.) No network request to api.github.com (`adb logcat`, Wi-Fi traffic).
- [x] No "Update available" pill on Home.
- [x] Rider, focus: Settings › Look and motion › Transition speed: Left from "−" stays on "−". Same on the first chip of Glass, Clock, °C/°F, the first currency, the Wallpaper mode chips, "Move left" of a pinned app, and the Weather buttons. Up from the first row still reaches Home, and OK there closes Settings.
- [x] Rider, reminders: with a TEST subscription due inside the lead time and a reminder posted, turning renewal reminders off withdraws the reminder. (An update notification cannot be posted from the dev app; the rule is covered by `cancelAllReminders` filtering the renewal channel.)

Result 2026-11-17, KS TV Dev 0.6.1 (17) debug, owner's TV (KTC Lumio Vision, Android 14): About shows the test-build note and no update controls; no pill on Home. Left held on "−" of Transition speed (3 presses), "Follow TV", "Detect again", Netflix "Move left" and "INR"; Up from the Profile row reached Home. TEST Netflix (renews today) gave a reminder on the `renewals` channel; turning reminders off withdrew it (0 KS Dev notifications left). TEST item deleted, reminders left off as found. Not checked: network traffic to api.github.com (no capture tool on the TV).

### Fix after v0.7.0: About update rows (KS TV Dev, from now on)
- [x] About shows, in order: version, device, "Check for updates automatically", "Check for updates" + "Download and install", "This is a test build…", "Android settings".
- [x] Down from the switch → "Check for updates"; Down again → "Android settings"; Up from "Android settings" → "Check for updates"; Up again → the switch.
- [x] Right from "Check for updates" → "Download and install"; Left back → "Check for updates"; Left again stays; Up from "Download and install" → the switch; Down → "Android settings".
- [x] OK on the switch, "Check for updates" and "Download and install" each shows "Updates are off in KS TV Dev." and changes nothing (switch stays on; no pill on Home; no request to api.github.com).
- [ ] Release only: the same paths in KS TV (the action button reads Cancel during a download and Install while waiting for permission, in the same place).
Result 2026-11-17, KS TV Dev 0.7.0 (18) debug, owner's TV: rows in the release order, preview "Download and install" beside "Check for updates". All eight paths above as listed (Down/Up through switch → Check → Android settings and back, Right/Left between the buttons, Left stops on Check, Up/Down from "Download and install"). OK on Check, Download and install and the switch each showed "Updates are off in KS TV Dev."; the switch stayed on; Up from the switch reached "Remind me before renewals"; no pill on Home; no KS Dev notifications. The release APK built from this tree has no "KS TV Dev" text (`aapt2 dump strings`). First-run profile dialog in the fresh dev install was cancelled; nothing saved.

### On a release build (needs release N on the TV and N+1 published)
- [ ] About shows "Check for updates automatically" (on) and "Check for updates". With no newer release: "KS TV is up to date."
- [ ] With Wi-Fi off: "Could not check for updates…"; KS keeps working.
- [ ] After N+1 is published: "Available: vN+1", the explanation line and the release notes; Home shows the "Update available" pill; OK on the pill opens Settings at About with focus on "Check for updates".
- [ ] Daily check (or WorkManager forced run) posts "KS TV update available: vX.Y.Z" on the "App updates" channel, once per version; opening it shows Settings › About.
- [ ] Turning renewal reminders off leaves the update notification in place; turning automatic checks off leaves reminders working.
- [ ] Download and install: progress in %, Cancel stops it and deletes the file (`run-as` not possible on release: check "Downloading" disappears and nothing installs).
- [ ] First time: Android's "Install unknown apps" page for KS opens; after allowing and pressing Back the install continues; Android asks to confirm; Cancel there says "Update cancelled. KS was not changed."
- [ ] Confirm: KS updates in place. After reopening, About shows vN+1 and no pill. Names, active name, TV name, pins and order, subscriptions and totals, weather mode and city, wallpaper and settings are unchanged (record date and versions).

