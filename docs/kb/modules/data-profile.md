---
type: Module
title: data.profile
package: com.ks.tvlauncher.data.profile
spec: [../../../specs/greeting-widget.md]
---

# data.profile

## Purpose
Saved viewer names and the active name ("who's watching"), stored as a JSON array in the shared Preferences DataStore.

## Rules
- Names always pass through `ProfileNames.sanitize` (max 8 names, 1-20 chars).
- Switching to the next name wraps round.
- Names never leave the device.
- `save` writes the whole profile dialog in one edit and sets `setupDone`; `markSetupDone` is for Cancel on the first-run dialog. Home opens the dialog by itself only while `!setupDone` and no names are saved.

<!-- kb:generated:start -->

## Files (generated)
- `data/profile/ProfileRepository.kt` — `Profile`, `ProfileRepository`

Tests: `data/profile/ProfileRepositoryTest.kt`

## Depends on (generated)
- [domain](domain.md) via `ProfileNames`

## Used by (generated)
- [root](root.md) via `ProfileRepository`
- [ui.home](ui-home.md) via `Profile`, `ProfileRepository`
- [ui.settings](ui-settings.md) via `Profile`, `ProfileRepository`

## Spec
- [greeting-widget](../../../specs/greeting-widget.md)

<!-- kb:generated:end -->
