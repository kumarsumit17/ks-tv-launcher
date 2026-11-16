# Spec log

## 2026-11-16
- Split the single `docs/SPEC.md` into this OKF v0.1 bundle, one concept per file, with a per-stage reading table in `index.md`.
- Added [token-budget](token-budget.md) and `code-map.md` (Graphify-style map to read before searching code).
- Added [greeting-widget](greeting-widget.md) and [memory-storage-widget](memory-storage-widget.md).
- Renamed the RAM panel button from "Running apps" to "Manage apps".
- Merged from the earlier repo notes: CSV export contract with `ks-expense-manager` ([subscriptions](subscriptions.md)), blur budget ([design-system](design-system.md)), no long-press-only actions ([apps](apps.md)), device-only data, key-mapper alternative ([google-tv-setup](google-tv-setup.md)).
- Dropped from the earlier notes (superseded by this spec): Hilt, Retrofit/OkHttp, OpenWeatherMap + API key, the `di/` package, and the 16sp minimum body size (the approved mockups' type scale applies).
- Renamed mockups to short names (Home.png, AllApps.png, Subscriptions.png, ControlCenter.png, Flow.png, Greeting.png, MemoryStorage.png).
- Added [performance](performance.md) from the owner's performance and lightweight-design requirements; linked from `index.md`.
- Stage 1 done: filled in `code-map.md`; added `docs/MANUAL_TEST.md`. Haze 2.x splits blur into `dev.chrisbanes.haze:haze-blur`, which the build uses alongside `haze`.
- Replaced the hand-written code map with an OKF code bundle `docs/kb/` and `scripts/kb.py` (build, query, check); see [knowledge-graph](knowledge-graph.md). [token-budget](token-budget.md), [definition-of-done](definition-of-done.md) and [ground-rules](ground-rules.md) now require `kb.py query` before searching and a passing `kb.py check` before each commit. `code-map.md` is a redirect, to be removed.
- Added [app-mode](app-mode.md): KS runs as an app opened from the owner's launcher (`com.sumit.lumiohome`), never the default Home. Manifest drops HOME/DEFAULT, Back on the main screen moves KS to the background, tile label "KS TV". [google-tv-setup](google-tv-setup.md) no longer covers disabling the stock launcher. Implemented at the start of Stage 2.
- Stage 2: [app-config](app-config.md) now lists the `REQUEST_DELETE_PACKAGES` permission, which `ACTION_DELETE` in the app menu ([apps](apps.md)) needs on API 28+.
