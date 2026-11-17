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
- [ ] Each tile opens its Android settings page; Screensaver starts the screensaver or opens its settings.
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
