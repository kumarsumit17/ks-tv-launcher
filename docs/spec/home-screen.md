---
type: Screen
title: Home screen
stage: 1-2
mockups: [Home.png]
---

# Home screen

Top to bottom:

1. **Top bar**: "KS" badge + "TV Launcher"; clock (minute-boundary ticker flow, no per-second polling; 12/24h per [settings](settings.md)); short date; weather pill ([weather](weather.md), OK → Settings at Weather); gear → KS [settings](settings.md) (Android settings are a row there and in [control-center](control-center.md)).
2. **Greeting card**: see [greeting-widget](greeting-widget.md).
3. **Hero (focused app)**: app name large, one line of context, buttons "Open ‹app›" (primary) and "Unpin". Background: [dynamic-wallpaper](dynamic-wallpaper.md).
4. **Pinned row**: `LazyRow` of 160×90dp banner tiles, 16dp gap; only the focused tile shows its label; the row keeps the focused tile fully visible. Data: [apps](apps.md).
5. **Bottom strip**, four glass cards: "All apps (N installed)" · "Active Subscriptions" (monthly, yearly, active count, next-renewal pill; [subscriptions](subscriptions.md)) · "Memory & storage" ([memory-storage-widget](memory-storage-widget.md)) · compact "Control Center" ([control-center](control-center.md)).

Stage 1 builds the shell (top bar, greeting, hero, empty row placeholders). Stage 2 fills the row and wallpaper.
