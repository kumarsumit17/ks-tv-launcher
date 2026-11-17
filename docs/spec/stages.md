---
type: Plan
title: Stages
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

Every stage ends with [definition-of-done](definition-of-done.md), including `kb.py build` and `kb.py check` ([knowledge-graph](knowledge-graph.md)), and [verification](verification.md) before any commit.
