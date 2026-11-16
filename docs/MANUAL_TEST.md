# Manual test checklist

On-device checks that unit tests cannot cover. Use a real TV or the Android TV emulator (1080p); phone emulators hide focus bugs. Install with `./gradlew installDebug`.

## Stage 1: Foundation

### Launcher registration
- [ ] The app appears in the TV's app list with the "KS" banner.
- [ ] `adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME` offers or opens KS - TV Launcher.
- [ ] Optional, with a device attached: `./gradlew connectedDebugAndroidTest` passes (`HomeIntentTest`).

### Home shell
- [ ] Cold start shows a dark navy screen at once, with no white flash.
- [ ] Top bar shows the "KS" badge, "TV Launcher", the time, the short date, a dimmed "Weather not set up" pill and the gear.
- [ ] The clock changes at the minute boundary, within a second or two.
- [ ] Changing the TV's time, time zone or 12/24-hour setting updates the clock without restarting the app.
- [ ] The greeting matches the time of day: morning 5:00–11:59, afternoon 12:00–16:59, evening 17:00–4:59.
- [ ] With no name saved, the greeting has no name after it.
- [ ] The second line is the long date in the TV's language.
- [ ] The third line shows the TV's name from Android Settings › Device name and the correct Android version.
- [ ] Renaming the TV in Android settings updates the third line when you return to Home.
- [ ] Text is fully inside the screen edges (no overscan clipping).

### D-pad
- [ ] On launch, focus is on the first slot of the pinned row.
- [ ] A focused slot grows slightly and shows a white border and glow; it shrinks back when focus leaves.
- [ ] Left and Right move through the six slots; the row scrolls so the focused slot is fully visible.
- [ ] Up from the row reaches the gear; Down from the gear returns to the same slot.
- [ ] The weather pill is skipped by the D-pad.
- [ ] OK on the gear opens Android settings; Back returns to Home with focus on the gear.
- [ ] Back on Home does nothing.
- [ ] Pressing Home while on Home scrolls the row to the start and focuses the first slot.

### Motion and effects
- [ ] With Developer options › Animator duration scale set to "Animation off", focus changes are instant.
- [ ] On an API 31+ device the weather pill looks frosted; on older devices it is plain translucent glass.

### Persistence
- [ ] After a reboot, Home starts as the launcher (if it was set as Home) and shows the same state.

_Not yet testable: the profile switch chip (needs two saved names; the profile dialog arrives in Stage 4) and `GlassDialog`, `GlassToggle`, `GlassStepper`, `AppTile` (built in Stage 1, first used on screen in later stages)._
