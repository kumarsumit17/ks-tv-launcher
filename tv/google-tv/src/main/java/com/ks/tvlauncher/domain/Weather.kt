package com.ks.tvlauncher.domain

import kotlin.math.floor

/** The icon groups the weather pill uses; every WMO weather code maps to one. */
enum class WeatherGroup { CLEAR, CLOUDY, FOG, RAIN, SNOW, STORM }

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }

object WeatherCodes {
    /**
     * WMO weather interpretation codes, as Open-Meteo returns them, grouped for the icons:
     * 0–1 clear, 2–3 cloudy, 45/48 fog, drizzle/rain/showers rain, snow and snow showers snow,
     * 95–99 thunderstorm. Unknown codes count as cloudy.
     */
    fun group(code: Int): WeatherGroup = when (code) {
        0, 1 -> WeatherGroup.CLEAR
        2, 3 -> WeatherGroup.CLOUDY
        45, 48 -> WeatherGroup.FOG
        in 51..67, in 80..82 -> WeatherGroup.RAIN
        in 71..77, 85, 86 -> WeatherGroup.SNOW
        in 95..99 -> WeatherGroup.STORM
        else -> WeatherGroup.CLOUDY
    }

    /** Whole degrees in [unit] from a Celsius reading, rounded half up. */
    fun temperature(celsius: Double, unit: TemperatureUnit): Int = when (unit) {
        TemperatureUnit.CELSIUS -> roundHalfUp(celsius)
        TemperatureUnit.FAHRENHEIT -> roundHalfUp(celsius * 9.0 / 5.0 + 32.0)
    }

    private fun roundHalfUp(value: Double): Int = floor(value + 0.5).toInt()
}

/** A place chosen in Settings (TVs have no GPS). Stored on the device only. */
@kotlinx.serialization.Serializable
data class WeatherCity(
    val name: String,
    /** State or region, may be empty. */
    val region: String = "",
    val country: String = "",
    val latitude: Double,
    val longitude: Double,
) {
    /** "Pune, Maharashtra, India" without empty parts. */
    val description: String get() = listOf(name, region, country).filter { it.isNotBlank() }.distinct().joinToString(", ")

    /** Identifies the place for the weather cache. */
    val key: String get() = "%.3f,%.3f".format(java.util.Locale.ROOT, latitude, longitude)
}

/** AUTO: the city is detected from the network (default). MANUAL: the user picked one. */
enum class WeatherCityMode { AUTO, MANUAL }
