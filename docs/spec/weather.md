---
type: Feature
title: Weather
stage: 4
---

# Weather (`data/weather/`)

- Provider **Open-Meteo**, no API key:
  - Geocoding: `https://geocoding-api.open-meteo.com/v1/search?name=<city>&count=5`
  - Forecast: `https://api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current=temperature_2m,weather_code`
- Behind a `WeatherProvider` interface; any future provider key goes in `local.properties` → `BuildConfig`, never committed.
- City chosen in [settings](settings.md) (no GPS on TVs). No city → pill shows "Set city" and opens Settings.
- Refresh every 30 min while Home is visible; cache the last result.
- States: loading (shimmer pill); error (last cached value dimmed, or "Weather unavailable"). Map WMO codes to ~6 icon groups.
