---
type: Process
title: Definition of done
stage: all
---

# Definition of done (every stage)

1. `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` pass. Fix and rerun until they do.
2. Unit tests (add as features land):
   - Subscription calculator: monthly/yearly maths, rounding, active filtering, renewal roll-forward, days-until.
   - Repository + DataStore round-trip (temp-file DataStore, `runTest`), including corrupt JSON.
   - Pinned apps: persistence and uninstall pruning.
   - Greeting: boundaries 4:59/5:00, 11:59/12:00, 16:59/17:00; no-name fallback.
   - Profile names: trimming, length limits, max count, next-name wrap-around.
   - Byte formatting and the 75 % / 90 % thresholds (exactly 75.0 % and 90.0 %).
   - `SubscriptionCsvTest`: header, quoting, CRLF, `amount_minor`, `next_due` roll-forward.
3. Knowledge graph ([knowledge-graph](knowledge-graph.md)): run `python3 scripts/kb.py build`; write Purpose and Rules for any new module and update Rules that changed; add an entry to `docs/kb/log.md`; `python3 scripts/kb.py check` must print OK. Update `README.md` if the stack or setup changed (it must not mention Hilt, Retrofit or OpenWeatherMap).
4. Add this stage's on-device checks to `docs/MANUAL_TEST.md` (D-pad paths, focus return, reboot persistence, uninstall while pinned).
5. `git commit`, one commit per stage.
6. Short report: files created/changed, dependencies added (versions), real command results, APK path, anything not done and why.
