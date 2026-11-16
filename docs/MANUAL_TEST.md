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

