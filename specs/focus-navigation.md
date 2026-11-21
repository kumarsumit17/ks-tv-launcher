---
type: Specification
title: Remote and focus rules
description: "D-pad rules, `animateDpadFocus`."
tags: [focus, navigation, behavior]
stage: 1-2
mockups: [Flow.png]
---

# Remote and focus rules (critical)

## `Modifier.animateDpadFocus(glow: Color = White)`
- Focused: scale 1.06, 3dp white border at 95 %, raised shadow, soft glow in `glow`. Unfocused: animate back.
- Built on `onFocusChanged` + `animateFloatAsState`/`animateDpAsState` + `graphicsLayer`. No per-frame recomposition.

## Navigation
- Works with D-pad, OK, Back, Home, and MENU where present. No touch-only UI.
- Initial focus: first pinned tile, via `FocusRequester` after the first frame.
- `focusGroup()` per section, `focusRestorer()` on rows/grids, `focusProperties { up/down }` so Up/Down move predictably: top bar → greeting chip → hero buttons → pinned row → bottom strip.
- **Top bar ends**: Left on the first control of the top bar (the update pill, else the weather pill) and Right on the gear stay put. Nothing focusable sits beside them in the bar, so the focus search must not jump to the bottom strip.
- Opening an overlay (drawer, dialog, Control Center, Settings) moves focus into it; closing returns focus to the control that opened it.
- **Open dialogs and menus hold focus**: the D-pad cannot leave a `GlassDialog`, and focus never reaches the controls behind it. When the focused button disappears while the dialog stays open (Move right at the end of the pinned row, Move left at the start), focus goes to the nearest remaining button: the other Move button, else Unpin. If focus still ends up outside, the dialog takes it back within a frame.
- **Back**: closes the top overlay. On the main screen with nothing open: `moveTaskToBack(true)` to return to the owner's launcher ([app-mode](app-mode.md)).
- **Home key**: left to the system. Reopening KS (`onNewIntent`): close any open screen or overlay and return focus to the control that opened it; with nothing open, to the last focused tile ([app-mode](app-mode.md)).
- No focus traps; every control is reachable and can be left in every direction it visually allows.
