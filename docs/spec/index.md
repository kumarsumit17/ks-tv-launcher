---
okf_version: "0.1"
---

# KS - TV Launcher spec

An ad-free, Apple TV–style home screen for Google TV / Android TV, fully controlled by the remote. Build it in stages; do only the stage you are asked for.

## Read for each stage

Always read [ground-rules](ground-rules.md), [performance](performance.md) and [definition-of-done](definition-of-done.md), and [code-map](code-map.md) from Stage 2 on. Then only:

| Stage | Read these | Mockups |
|---|---|---|
| 1 Foundation | [tech-stack](tech-stack.md), [app-config](app-config.md), [design-system](design-system.md), [focus-navigation](focus-navigation.md), [home-screen](home-screen.md), [greeting-widget](greeting-widget.md), [architecture](architecture.md) | Home, Greeting |
| 2 Apps | [apps](apps.md), [dynamic-wallpaper](dynamic-wallpaper.md), [focus-navigation](focus-navigation.md), [home-screen](home-screen.md) | Home, AllApps, Flow |
| 3 Subscriptions | [subscriptions](subscriptions.md) | Subscriptions |
| 4 Control & widgets | [control-center](control-center.md), [settings](settings.md), [weather](weather.md), [greeting-widget](greeting-widget.md), [memory-storage-widget](memory-storage-widget.md) | ControlCenter, Greeting, MemoryStorage |
| 5 Polish | [reminders](reminders.md), [google-tv-setup](google-tv-setup.md) | none |

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
- [google-tv-setup](google-tv-setup.md): making KS the Home app
- [architecture](architecture.md): packages, MVVM rules
- [stages](stages.md): the five stages
- [definition-of-done](definition-of-done.md): build, tests, commit, report
- [code-map](code-map.md): where the code lives (Claude keeps it updated)

Changes to this spec are recorded in [log](log.md).
