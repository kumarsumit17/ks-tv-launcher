---
type: Module
title: data.profile
package: com.ks.tvlauncher.data.profile
spec: [../../spec/greeting-widget.md]
---

# data.profile

## Purpose
Saved viewer names and the active name ("who's watching"), stored as a JSON array in the shared Preferences DataStore.

## Rules
- Names always pass through `ProfileNames.sanitize` (max 8 names, 1-20 chars).
- Switching to the next name wraps round.
- Names never leave the device.

<!-- kb:generated:start -->

## Files (generated)
- `data/profile/ProfileRepository.kt` — `Profile`, `ProfileRepository`

## Depends on (generated)
- [domain](domain.md) via `ProfileNames`

## Used by (generated)
- [root](root.md) via `ProfileRepository`
- [ui.home](ui-home.md) via `Profile`, `ProfileRepository`

## Spec
- [greeting-widget](../../spec/greeting-widget.md)

<!-- kb:generated:end -->
