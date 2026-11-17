---
type: Module
title: data.weather
package: com.ks.tvlauncher.data.weather
spec: [../../spec/weather.md]
---

# data.weather

## Purpose
Weather for the current city: `CityLocator` / `IpWhoIsLocator` (automatic city from the public IP), `CityResolver` (AUTO vs MANUAL city, detection cache), `WeatherProvider` / `OpenMeteoProvider` (geocoding + forecast, no API key) and `WeatherRepository` (`WeatherState` flow, 30-minute refresh while collected, last reading cached).

## Rules
- No Retrofit, Ktor or OpenWeatherMap. A future keyed provider reads its key from `local.properties` → `BuildConfig`; never commit keys.
- City mode AUTO (default) uses `https://ipwho.is/` (HTTPS only, never `http://`); MANUAL uses the Open-Meteo search result the user picked. No GPS, no location permission.
- Detection runs on first launch, then at most once per 24 h while the city is collected, and on "Detect again"; a `Mutex` plus the cache check stops two collectors from detecting twice.
- Failed detection keeps the last detected city; with none, the city is null and the pill shows "Set city".
- Only the TV's public IP (AUTO), the typed city name and the chosen coordinates are sent.
- Refresh happens only while `state` is collected (Home visible): a fresh cache is not refetched; after a failure, retry in 5 minutes.
- Failure with a cached reading → `Ready(stale = true)` (pill dimmed); with none → `Unavailable`. A corrupt cache is ignored, never thrown.
- Parsing lives in `OpenMeteoProvider.parseGeocoding` / `parseForecast` so it is unit-tested without the network.

<!-- kb:generated:start -->

## Files (generated)
- `data/weather/CityLocator.kt` — `CityLocator`, `IpWhoIsLocator`
- `data/weather/CityResolver.kt` — `CityResolver`, `ResolvedCity`
- `data/weather/OpenMeteoProvider.kt` — `OpenMeteoProvider`
- `data/weather/WeatherProvider.kt` — `CurrentWeather`, `WeatherProvider`
- `data/weather/WeatherRepository.kt` — `WeatherRepository`, `WeatherState`

Tests: `data/weather/CityResolverTest.kt`, `data/weather/IpWhoIsLocatorTest.kt`, `data/weather/OpenMeteoParsingTest.kt`, `data/weather/WeatherRepositoryTest.kt`

## Depends on (generated)
- [data.settings](data-settings.md) via `LauncherSettings`
- [domain](domain.md) via `WeatherCity`, `WeatherCityMode`

## Used by (generated)
- [root](root.md) via `CityResolver`, `IpWhoIsLocator`, `OpenMeteoProvider`, `WeatherRepository`
- [ui.home](ui-home.md) via `CityResolver`, `ResolvedCity`, `WeatherRepository`, `WeatherState`
- [ui.settings](ui-settings.md) via `CityResolver`, `ResolvedCity`, `WeatherRepository`

## Spec
- [weather](../../spec/weather.md)

<!-- kb:generated:end -->
