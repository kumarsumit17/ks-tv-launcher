---
type: Specification
title: Weather
description: "Open-Meteo, city, states."
tags: [weather, feature]
stage: 4
---

# Weather (`data/weather/`)

- Provider **Open-Meteo**, no API key:
  - Geocoding: `https://geocoding-api.open-meteo.com/v1/search?name=<city>&count=5`
  - Forecast: `https://api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current=temperature_2m,weather_code`
- Behind a `WeatherProvider` interface; any future provider key goes in `local.properties` → `BuildConfig`, never committed.
- **City is automatic by default**, so weather shows as soon as the APK is installed, with nothing to set up. Details below.

## City: automatic by default, manual override
- Setting `weatherCityMode`: `AUTO` (default) or `MANUAL`.
- **AUTO**: TVs have no GPS, so the city comes from the network's public IP. One HTTPS GET to `https://ipwho.is/` (no API key; fields used: `success`, `city`, `region`, `country`, `latitude`, `longitude`). Use the returned latitude/longitude directly for the forecast and show `city` in the pill.
  - Behind a `CityLocator` interface so the service can be swapped. Before coding, confirm the endpoint answers over HTTPS from this network; if it does not, pick another keyless HTTPS service and record the change in [log](log.md). Never use a cleartext (`http://`) endpoint.
  - When it runs: on first launch, then at most once every 24 h, and when the user chooses "Detect again". Cache city + coordinates in DataStore; weather refreshes reuse the cache.
  - If detection fails: keep the last detected city; with none, the pill shows "Set city" and opens Settings.
- **MANUAL**: the user types a city in [settings](settings.md); it is resolved with the Open-Meteo geocoding search (pick from up to 5 matches) and used until they switch back. Choosing "Use my current city (automatic)" returns to AUTO.
- IP-based location is approximate (often the ISP's hub city), which is why the manual override exists. Settings shows which mode is active, e.g. "Bengaluru · automatic".
- Privacy: AUTO sends only the TV's public IP address to the lookup service, as any web request does. No names, apps or subscription data leave the device. Say this in one line under the setting.
- Refresh every 30 min while Home is visible; cache the last result.
- States: loading (shimmer pill); error (last cached value dimmed, or "Weather unavailable"). Map WMO codes to ~6 icon groups.
