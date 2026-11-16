---
type: Behavior
title: Remote and focus rules
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
- Opening an overlay (drawer, dialog, Control Center, Settings) moves focus into it; closing returns focus to the control that opened it.
- **Back**: closes the top overlay. On the main screen with nothing open: `moveTaskToBack(true)` to return to the owner's launcher ([app-mode](app-mode.md)).
- **Home key**: left to the system. Reopening KS (`onNewIntent`): close overlays and restore focus to the last focused tile.
- No focus traps; every control is reachable and can be left in every direction it visually allows.
