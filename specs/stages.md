---
type: Plan
title: Stages
description: "The five stages."
tags: [stages, plan]
stage: all
---

# Stages

1. **Foundation**: inspect repo; Gradle + catalog; [app-config](app-config.md); banner/icon; [design-system](design-system.md) + `animateDpadFocus`; Home shell ([home-screen](home-screen.md)) with live clock and [greeting-widget](greeting-widget.md) lines 1–3.
2. **Apps**: first switch to [app-mode](app-mode.md) (manifest without HOME/DEFAULT, Back → `moveTaskToBack`, short tile label, update `HomeIntentTest`, `MANUAL_TEST.md` and `README.md`); then [apps](apps.md), [dynamic-wallpaper](dynamic-wallpaper.md), full [focus-navigation](focus-navigation.md).
3. **Subscriptions**: [subscriptions](subscriptions.md) with unit tests.
4. **Control & widgets**: [control-center](control-center.md), [settings](settings.md), [weather](weather.md), profile dialog ([greeting-widget](greeting-widget.md)), [memory-storage-widget](memory-storage-widget.md).
5. **Polish**: [reminders](reminders.md), performance pass, empty/error states, [google-tv-setup](google-tv-setup.md).
   - Cold start: Home briefly shows "No pinned apps yet · 0 installed" before the first app scan finishes; show a loading state until apps have loaded (found in the Stage 4 TV test).
   - Text fields (`GlassSearchField`): the first Back only hides the keyboard and a second Back is needed to leave editing; one Back should do both (found in the Stage 4 TV test).
6. **Release readiness and extras**, all under [dev-workflow](dev-workflow.md) (separate KS TV Dev app, `dev` → `main` → tag, verify data after each release). Start with the [release-checklist](release-checklist.md) (finish Stage 5, release `v0.5.0`, move the TV to the release build, full on-device check, key backup). Then three independent parts, each tested, reviewed and committed on its own ([verification](verification.md)):
   - 6a [inputs](inputs.md)
   - 6b [custom-wallpaper](custom-wallpaper.md)
   - 6c, last, in three parts, each tested, reviewed by Cowork and committed on its own, in this order:
     1. [smoothness](smoothness.md) pass (measure on a `perf` build, find the cause, fix).
     2. [app-updates](app-updates.md) (needs checklist steps 3 and 5 confirmed by the owner).
     3. [final-check](final-check.md) (Stage 1–3 checklists on the release build, reminders after a TV restart), after 1 and 2 are released.
     - Also in 6c ([focus-navigation](focus-navigation.md)): in Settings, Left on the first control of a row must not jump to the Home button at the top-left. Seen on the TV after 6b: Left from "−" of Transition speed focused Home, and OK there closed Settings.

Every stage ends with [definition-of-done](definition-of-done.md), including `kb.py build` and `kb.py check` ([knowledge-graph](knowledge-graph.md)), and [verification](verification.md) before any commit.
