package com.ks.tvlauncher.data.weather

import com.ks.tvlauncher.domain.WeatherCity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * Open-Meteo (open-meteo.com): free, no API key, no account. Only the city the user typed and
 * the chosen place's coordinates are sent. Plain `HttpURLConnection` on [Dispatchers.IO].
 */
class OpenMeteoProvider : WeatherProvider {

    override suspend fun searchCity(name: String): List<WeatherCity> {
        val query = name.trim()
        if (query.isEmpty()) return emptyList()
        val encoded = URLEncoder.encode(query, "UTF-8")
        return parseGeocoding(get("$GEOCODING_URL?name=$encoded&count=5"))
    }

    override suspend fun current(city: WeatherCity): CurrentWeather {
        val lat = String.format(Locale.ROOT, "%.4f", city.latitude)
        val lon = String.format(Locale.ROOT, "%.4f", city.longitude)
        return parseForecast(get("$FORECAST_URL?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code"))
    }

    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/json")
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("HTTP $code")
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    @Serializable
    private data class GeocodingResponse(val results: List<GeoResult>? = null)

    @Serializable
    private data class GeoResult(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val country: String? = null,
        @SerialName("admin1") val region: String? = null,
    )

    @Serializable
    private data class ForecastResponse(val current: Current? = null)

    @Serializable
    private data class Current(
        @SerialName("temperature_2m") val temperature: Double,
        @SerialName("weather_code") val weatherCode: Int,
    )

    companion object {
        private const val GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search"
        private const val FORECAST_URL = "https://api.open-meteo.com/v1/forecast"
        private const val TIMEOUT_MS = 10_000

        private val json = Json { ignoreUnknownKeys = true }

        /** Parses a geocoding response. A response with no `results` means no match. */
        @Throws(IOException::class)
        fun parseGeocoding(body: String): List<WeatherCity> = decode(GeocodingResponse.serializer(), body)
            .results.orEmpty()
            .map { WeatherCity(it.name, it.region.orEmpty(), it.country.orEmpty(), it.latitude, it.longitude) }

        /** Parses a forecast response with `current=temperature_2m,weather_code`. */
        @Throws(IOException::class)
        fun parseForecast(body: String): CurrentWeather {
            val current = decode(ForecastResponse.serializer(), body).current
                ?: throw IOException("No current weather in response")
            return CurrentWeather(current.temperature, current.weatherCode)
        }

        private fun <T> decode(serializer: kotlinx.serialization.KSerializer<T>, body: String): T = try {
            json.decodeFromString(serializer, body)
        } catch (e: SerializationException) {
            throw IOException("Unexpected weather response", e)
        } catch (e: IllegalArgumentException) {
            throw IOException("Unexpected weather response", e)
        }
    }
}
