---
type: Specification
title: Inputs (HDMI sources)
description: "HDMI and other inputs from Control Center (Stage 6)."
tags: [inputs, feature]
stage: 6
---

# Inputs (HDMI sources)

An "Inputs" tile in [control-center](control-center.md) that lists the TV's inputs and switches to one, like the owner's own launcher does.

- **List**: `TvInputManager.getTvInputList()`, keeping inputs where `isPassthroughInput()` is true (HDMI, AV, component). Label = `loadCustomLabel` ?: `loadLabel`. Show whether each is connected (`getInputState`), and refresh through `TvInputManager.TvInputCallback` only while the panel is open (no polling, [performance](performance.md)).
- **Switch**: `Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(inputId))` with `FLAG_ACTIVITY_NEW_TASK`, started through the safe-start helper.
- **UI**: OK on the tile opens a `GlassDialog` list of inputs; OK on one switches; Back closes and returns focus to the tile. With no passthrough inputs the tile is hidden; if the switch intent cannot be started, show a glass snackbar.
- No new permission is expected. Confirm on the owner's TV (Lumio Vision 9, Android 14) before building further: if the list is empty or switching fails there, stop and report instead of guessing an alternative API.
- Pure logic (filtering, ordering, labels) lives in `domain/` with unit tests.
