---
type: Architecture
title: Architecture
stage: 1
---

# Architecture

```
com.ks.tvlauncher
  KsApplication, MainActivity, AppContainer
  data/{apps, subscriptions, settings, weather, system, profile}
  domain/   (SubscriptionCalculator, Greeting, ByteFormatter, pure models)
  ui/{theme, components, home, apps, subscriptions, controlcenter, settings, widgets}
  util/
```

- One ViewModel per screen exposing `StateFlow<…UiState>`; immutable state.
- Small composables, one concern per file, stateless where practical; `collectAsStateWithLifecycle`.
- All I/O on IO dispatchers. Keep the [knowledge-graph](knowledge-graph.md) in sync.
