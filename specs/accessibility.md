---
type: Specification
title: Accessibility (TalkBack on TV, contrast, motion)
description: What KS does so TalkBack on TV can read every control, how it follows the system's high-contrast text and Remove animations settings, and how labels are checked.
tags: [accessibility, talkback, contrast, motion, testing]
---

# Accessibility (TalkBack on TV, contrast, motion)

KS is used from the sofa with a remote, and some people use it with TalkBack on TV. These rules come on top of [focus-navigation](focus-navigation.md) (D-pad first, a visible focus on every focusable element, never long-press only).

## TalkBack
- **Every focusable element has words.** Text controls are read from their text. Icon-only controls carry a `contentDescription` (`GlassIconButton` and the stepper's buttons require one). App tiles carry the app's name. Toggles have `Role.Switch` and a state ("On"/"Off"); chips report `selected`.
- **A card is read as one item.** A focusable card's children are its label (TalkBack reads "Active Subscriptions 0, Track what you pay for"), so cards keep their text inside the focusable element.
- **Nothing hidden is reachable.** While an overlay (All apps, Subscriptions, Settings, Control Center, a dialog or a menu) covers Home, the Home content is cleared from the accessibility tree (`clearAndSetSemantics` on Home's column while `anyOverlay`), so TalkBack's own navigation cannot land on controls behind it. D-pad focus is not affected.
- **Focus order is the visual order**, as the D-pad paths in [focus-navigation](focus-navigation.md) define it; TalkBack on TV moves its focus with the D-pad along the same paths.

## System settings KS follows (live)
`rememberSystemAccessibility()` in `ui.theme` reads these and updates while KS is open, through a content observer (and Android 14+'s contrast listener). No polling.
- **Remove animations** (the animator duration scale at 0): no animations, the same as KS's own Reduce motion ([design-system](design-system.md), [settings](settings.md)).
- **High contrast text** (`high_text_contrast_enabled`), or a raised system contrast on Android 14+: every glass surface gets a dark backing (`GlassStyle.backingAlpha`, 62 % black under the fill), so all text on glass has strong contrast over any wallpaper. Android also draws its own high-contrast text treatment.
- **Text size**: the system font scale is followed up to 1.3× ([design-system](design-system.md), `KsCanvas`).
- **Language**: [languages](languages.md).

## Checking
- **Label audit** (`scripts/a11y_audit.py`, a development tool): dump a screen with `adb shell uiautomator dump`, pull it, and run the script; it lists every focusable element with no text or description on it or inside it, and exits 1 if there is one. Run it on Home, All apps, Subscriptions (with the add dialog), Control Center and Settings for every change that adds a control. Known false positive: a control clipped at the edge of a scrolling list (its text is outside the visible part); check the screenshot.
- **TalkBack pass** on the TV emulator: turn TalkBack on (`settings put secure enabled_accessibility_services com.google.android.marvin.talkback/.TalkBackService`, `accessibility_enabled 1`), move with the D-pad, check that its green focus follows and nothing crashes, then turn it off. The emulator's speech may not start, so what TalkBack says is checked by the label audit; listening to it on the owner's TV is a hand check that never blocks a release.
- `uiautomator dump` makes KS's window lose and regain focus, which KS treats like a reopen (focus back to the pinned row), so navigate with keys first and dump last.
