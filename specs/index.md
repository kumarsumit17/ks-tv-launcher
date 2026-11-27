---
okf_version: "0.2"
---

# KS - TV Launcher spec

An ad-free, Apple TV–style launcher app for Google TV / Android TV, fully controlled by the remote. It runs as an app opened from the TV's own launcher, never as the default Home ([app-mode](app-mode.md)). Build it in stages; do only the stage you are asked for.

# Read for each stage

Always read [ground-rules](ground-rules.md), [performance](performance.md), [definition-of-done](definition-of-done.md) and [verification](verification.md), and [knowledge-graph](knowledge-graph.md); look code up with `python3 scripts/kb.py query` before reading it. Then only:

| Stage | Read these | Mockups |
|---|---|---|
| 1 Foundation | [tech-stack](tech-stack.md), [app-config](app-config.md), [design-system](design-system.md), [focus-navigation](focus-navigation.md), [home-screen](home-screen.md), [greeting-widget](greeting-widget.md), [architecture](architecture.md) | Home, Greeting |
| 2 Apps | [app-mode](app-mode.md), [apps](apps.md), [dynamic-wallpaper](dynamic-wallpaper.md), [focus-navigation](focus-navigation.md), [home-screen](home-screen.md) | Home, AllApps, Flow |
| 3 Subscriptions | [subscriptions](subscriptions.md) | Subscriptions |
| 4 Control & widgets | [control-center](control-center.md), [settings](settings.md), [weather](weather.md), [greeting-widget](greeting-widget.md), [memory-storage-widget](memory-storage-widget.md) | ControlCenter, Greeting, MemoryStorage |
| 5 Polish | [reminders](reminders.md), [google-tv-setup](google-tv-setup.md) | none |
| 6 Release checklist | [release-checklist](release-checklist.md), [dev-workflow](dev-workflow.md), [verification](verification.md) | none |
| 6a Inputs | [inputs](inputs.md), [control-center](control-center.md) | none |
| 6b Wallpaper | [custom-wallpaper](custom-wallpaper.md), [dynamic-wallpaper](dynamic-wallpaper.md), [settings](settings.md) | none |
| 6c.1 Smoothness | [smoothness](smoothness.md), [performance](performance.md), [design-system](design-system.md), [dev-workflow](dev-workflow.md) | none |
| 6c.2 Updates | [app-updates](app-updates.md), [reminders](reminders.md), [settings](settings.md), [app-config](app-config.md) | none |
| 6c.3 Final check | [final-check](final-check.md), [release-checklist](release-checklist.md), [dev-workflow](dev-workflow.md), [smoothness](smoothness.md) (`perf` build), [reminders](reminders.md) | none |

**Status: complete as specified on 2026-11-17 at 16:00 IST (release `v0.7.3`).** For any later work, read [future-development](future-development.md) first.

Full stage list: [stages](stages.md). Mockups are in `docs/mockups/`. Where a mockup and this spec disagree, follow the spec.

# Product specifications

* [App configuration](app-config.md) - IDs, versioning, manifest
* [Design system](design-system.md) - colours, type, shapes, motion, components
* [Remote and focus rules](focus-navigation.md) - D-pad rules, `animateDpadFocus`
* [Home screen](home-screen.md) - Home layout, top to bottom
* [Greeting widget](greeting-widget.md) - greeting, name profiles, TV name + Android version
* [Memory and storage widget](memory-storage-widget.md) - RAM and storage, used / total
* [Dynamic wallpaper](dynamic-wallpaper.md) - background follows the focused app
* [Apps, pinned row and drawer](apps.md) - discovery, artwork, launching, pinned row, drawer
* [Subscriptions](subscriptions.md) - model, storage, maths, screen, dialog
* [Control Center](control-center.md) - slide-in quick panel
* [Settings](settings.md) - every persisted setting
* [Weather](weather.md) - Open-Meteo, city, states
* [Renewal reminders](reminders.md) - renewal notifications (Stage 5)
* [Inputs (HDMI sources)](inputs.md) - HDMI and other inputs from Control Center (Stage 6)
* [Custom wallpaper](custom-wallpaper.md) - built-in or user-picked background (Stage 6)
* [In-app updates](app-updates.md) - check, download, verify and install new releases, keeping data (Stage 6)
* [Languages (English and Hindi)](languages.md) - How KS speaks English and Hindi through ks-utility's language files, how the language is chosen and remembered, and how text in another script is laid out.
* [Accessibility (TalkBack on TV, contrast, motion)](accessibility.md) - What KS does so TalkBack on TV can read every control, how it follows the system's high-contrast text and Remove animations settings, and how labels are checked.

# Policies

* [Dependencies](dependencies.md) - Third-party dependencies are kept to the minimum; the order of preference and what must be reported for each new one.
* [Ground rules](ground-rules.md) - how to work in this repo
* [Token budget](token-budget.md) - how to keep Claude's token use low
* [Performance and lightweight design](performance.md) - lightweight design, background work, effect limits
* [App mode (not the default Home)](app-mode.md) - KS is an app, never the default Home
* [Future development rules](future-development.md) - rules for any change after completion, the signing key, open items
* [Code knowledge graph](knowledge-graph.md) - `docs/kb/` code graph, `scripts/kb.py`, OKF writing rules

# Plan

* [Stages](stages.md) - the five stages
* [Per-platform optimization plan](plan-per-platform.md) - Approved plan for making the Google TV build the best launcher app on its platform: D-pad and 10-foot UI, startup, size, Google TV rules, accessibility and languages.
* [Per-platform optimization plan, round 2](plan-per-platform-2.md) - The second round of Google TV optimizations for KS TV Launcher after round 1: an instant Home, smoother Settings and All apps, low-memory TVs, pictures from USB drives, and the hand checks on the owner's TV.

# Playbooks

* [TV setup](google-tv-setup.md) - installing KS and pinning it to favourites
* [Release checklist (finish Stage 5, move the TV to the release build)](release-checklist.md) - finish Stage 5, final release, move the TV to the release build, full check, key backup (Stage 6)
* [Dev app, branches and release cycle](dev-workflow.md) - separate KS TV Dev test app, `dev`/`main` branches, release cycle, data check after every release (Stage 6 on; read for every 6x part)
* [Smoothness pass](smoothness.md) - measured frame-time pass on a `perf` build, before the update work (Stage 6c part 1)
* [Final release check](final-check.md) - Stage 1–3 checklists (read-only on the real app, data entry on the `perf` build) and reminders after a TV restart (Stage 6c part 3, last)
* [Definition of done](definition-of-done.md) - build, tests, report
* [Build, test, release](verification.md) - Every feature or fix is built, tested on every TV-size emulator with sample data, then committed, pushed and released, without waiting for anyone.

# Reference

* [Tech stack](tech-stack.md) - fixed libraries and SDK levels
* [Architecture](architecture.md) - packages, MVVM rules

Changes to this spec are recorded in [log](log.md).
