package com.ks.tvlauncher.data.weather

import com.ks.tvlauncher.domain.WeatherCity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Finds the TV's approximate city. TVs have no GPS, so this uses the network. */
interface CityLocator {
    @Throws(IOException::class)
    suspend fun locate(): WeatherCity
}

/**
 * ipwho.is: keyless, HTTPS. Sends only the request itself (so the TV's public IP, as any web
 * request does); nothing else leaves the device. The result is approximate, often the ISP's
 * hub city, which is why Settings offers a manual city.
 */
class IpWhoIsLocator : CityLocator {

    override suspend fun locate(): WeatherCity = withContext(Dispatchers.IO) {
        val connection = URL(URL_STRING).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/json")
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("HTTP $code")
            parse(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    @Serializable
    private data class Response(
        val success: Boolean = false,
        val city: String? = null,
        val region: String? = null,
        val country: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
    )

    companion object {
        private const val URL_STRING = "https://ipwho.is/"
        private const val TIMEOUT_MS = 10_000
        private val json = Json { ignoreUnknownKeys = true }

        /** Reads `success`, `city`, `region`, `country`, `latitude`, `longitude`. Anything missing fails. */
        @Throws(IOException::class)
        fun parse(body: String): WeatherCity {
            val response = try {
                json.decodeFromString(Response.serializer(), body)
            } catch (e: SerializationException) {
                throw IOException("Unexpected location response", e)
            } catch (e: IllegalArgumentException) {
                throw IOException("Unexpected location response", e)
            }
            val city = response.city?.trim()
            if (!response.success || city.isNullOrEmpty() || response.latitude == null || response.longitude == null) {
                throw IOException("Location not found")
            }
            return WeatherCity(city, response.region.orEmpty(), response.country.orEmpty(), response.latitude, response.longitude)
        }
    }
}
