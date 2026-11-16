# Spec log

## 2026-11-16
- Split the single `docs/SPEC.md` into this OKF v0.1 bundle, one concept per file, with a per-stage reading table in `index.md`.
- Added [token-budget](token-budget.md) and [code-map](code-map.md) (Graphify-style map to read before searching code).
- Added [greeting-widget](greeting-widget.md) and [memory-storage-widget](memory-storage-widget.md).
- Renamed the RAM panel button from "Running apps" to "Manage apps".
- Merged from the earlier repo notes: CSV export contract with `ks-expense-manager` ([subscriptions](subscriptions.md)), blur budget ([design-system](design-system.md)), no long-press-only actions ([apps](apps.md)), device-only data, key-mapper alternative ([google-tv-setup](google-tv-setup.md)).
- Dropped from the earlier notes (superseded by this spec): Hilt, Retrofit/OkHttp, OpenWeatherMap + API key, the `di/` package, and the 16sp minimum body size (the approved mockups' type scale applies).
- Renamed mockups to short names (Home.png, AllApps.png, Subscriptions.png, ControlCenter.png, Flow.png, Greeting.png, MemoryStorage.png).
- Added [performance](performance.md) from the owner's performance and lightweight-design requirements; linked from `index.md`.
- Stage 1 done: filled in [code-map](code-map.md); added `docs/MANUAL_TEST.md`. Haze 2.x splits blur into `dev.chrisbanes.haze:haze-blur`, which the build uses alongside `haze`.
