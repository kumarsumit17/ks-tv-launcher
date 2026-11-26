---
type: Module
title: data.language
package: com.ks.tvlauncher.data.language
spec: [../../../specs/languages.md]
---

# data.language

## Purpose
The app's language choice (Same as the system, English, Hindi), applied through Android 13+'s per-app language or, on Android 8–12, KS's own preference and a wrapped context ([languages](../../../specs/languages.md)).

## Rules
- Anything unknown in a saved choice counts as "Same as the system" (`AppLanguage.parse`).
- On Android 8–12 both `MainActivity` and `KsApplication` must call `AppLanguage.wrap` in `attachBaseContext`, or notifications and the screen disagree.
- No AppCompat: the per-app language is the platform `LocaleManager`.

<!-- kb:generated:start -->

## Files (generated)
- `data/language/AppLanguage.kt` — `AppLanguage`

Tests: `data/language/AppLanguageTest.kt`

## Depends on (generated)
- nothing inside the app

## Used by (generated)
- [root](root.md) via `AppLanguage`
- [ui.settings](ui-settings.md) via `AppLanguage`

## Spec
- [languages](../../../specs/languages.md)

<!-- kb:generated:end -->
