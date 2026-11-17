---
okf_version: "0.1"
---

# KS - TV Launcher spec

An ad-free, Apple TV–style launcher app for Google TV / Android TV, fully controlled by the remote. It runs as an app opened from the TV's own launcher, never as the default Home ([app-mode](app-mode.md)). Build it in stages; do only the stage you are asked for.

## Read for each stage

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

Full stage list: [stages](stages.md). Mockups are in `docs/mockups/`. Where a mockup and this spec disagree, follow the spec.

## All concepts

- [ground-rules](ground-rules.md): how to work in this repo
- [token-budget](token-budget.md): how to keep Claude's token use low
- [performance](performance.md): lightweight design, background work, effect limits
- [tech-stack](tech-stack.md): fixed libraries and SDK levels
- [app-config](app-config.md): IDs, versioning, manifest
- [design-system](design-system.md): colours, type, shapes, motion, components
- [focus-navigation](focus-navigation.md): D-pad rules, `animateDpadFocus`
- [home-screen](home-screen.md): Home layout, top to bottom
- [greeting-widget](greeting-widget.md): greeting, name profiles, TV name + Android version
- [memory-storage-widget](memory-storage-widget.md): RAM and storage, used / total
- [dynamic-wallpaper](dynamic-wallpaper.md): background follows the focused app
- [apps](apps.md): discovery, artwork, launching, pinned row, drawer
- [subscriptions](subscriptions.md): model, storage, maths, screen, dialog
- [control-center](control-center.md): slide-in quick panel
- [settings](settings.md): every persisted setting
- [weather](weather.md): Open-Meteo, city, states
- [reminders](reminders.md): renewal notifications (Stage 5)
- [app-mode](app-mode.md): KS is an app, never the default Home
- [google-tv-setup](google-tv-setup.md): installing KS and pinning it to favourites
- [release-checklist](release-checklist.md): finish Stage 5, final release, move the TV to the release build, full check, key backup (Stage 6)
- [dev-workflow](dev-workflow.md): separate KS TV Dev test app, `dev`/`main` branches, release cycle, data check after every release (Stage 6 on; read for every 6x part)
- [inputs](inputs.md): HDMI and other inputs from Control Center (Stage 6)
- [custom-wallpaper](custom-wallpaper.md): built-in or user-picked background (Stage 6)
- [app-updates](app-updates.md): check, download, verify and install new releases, keeping data (Stage 6)
- [smoothness](smoothness.md): measured frame-time pass on a `perf` build, before the update work (Stage 6c part 1)
- [final-check](final-check.md): Stage 1–3 checklists (read-only on the real app, data entry on the `perf` build) and reminders after a TV restart (Stage 6c part 3, last)
- [architecture](architecture.md): packages, MVVM rules
- [stages](stages.md): the five stages
- [definition-of-done](definition-of-done.md): build, tests, report
- [verification](verification.md): TV test with sample data, Claude Cowork review, then commit and push
- [knowledge-graph](knowledge-graph.md): `docs/kb/` code graph, `scripts/kb.py`, OKF writing rules

Changes to this spec are recorded in [log](log.md).
