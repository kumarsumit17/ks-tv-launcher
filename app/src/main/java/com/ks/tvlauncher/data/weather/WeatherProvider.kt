package com.ks.tvlauncher.data.weather

import com.ks.tvlauncher.domain.WeatherCity
import kotlinx.serialization.Serializable
import java.io.IOException

/** One reading: temperature in Celsius and the WMO weather code. */
@Serializable
data class CurrentWeather(val temperatureC: Double, val weatherCode: Int)

/**
 * A weather source. The app uses [OpenMeteoProvider], which needs no API key. A future
 * provider that needs a key reads it from `local.properties` through `BuildConfig`; keys are
 * never committed.
 */
interface WeatherProvider {
    /** Up to five places matching [name]. Empty when nothing matches. */
    @Throws(IOException::class)
    suspend fun searchCity(name: String): List<WeatherCity>

    @Throws(IOException::class)
    suspend fun current(city: WeatherCity): CurrentWeather
}
