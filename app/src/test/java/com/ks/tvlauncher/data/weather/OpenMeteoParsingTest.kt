package com.ks.tvlauncher.data.weather

import com.ks.tvlauncher.domain.WeatherCity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class OpenMeteoParsingTest {

    @Test
    fun geocodingResultsBecomeCities() {
        val body = """
            {"results":[
              {"id":1259229,"name":"Pune","latitude":18.51957,"longitude":73.85535,"country":"India","admin1":"Maharashtra","timezone":"Asia/Kolkata"},
              {"id":2,"name":"Pune","latitude":-8.0,"longitude":125.0}
            ],"generationtime_ms":0.5}
        """.trimIndent()
        assertEquals(
            listOf(
                WeatherCity("Pune", "Maharashtra", "India", 18.51957, 73.85535),
                WeatherCity("Pune", "", "", -8.0, 125.0),
            ),
            OpenMeteoProvider.parseGeocoding(body),
        )
    }

    @Test
    fun noResultsMeansNoMatch() {
        assertTrue(OpenMeteoProvider.parseGeocoding("""{"generationtime_ms":0.3}""").isEmpty())
    }

    @Test
    fun forecastCurrentReading() {
        val body = """
            {"latitude":18.5,"longitude":73.875,"current_units":{"temperature_2m":"°C"},
             "current":{"time":"2026-11-17T05:00","interval":900,"temperature_2m":27.4,"weather_code":3}}
        """.trimIndent()
        assertEquals(CurrentWeather(27.4, 3), OpenMeteoProvider.parseForecast(body))
    }

    @Test(expected = IOException::class)
    fun forecastWithoutCurrentFails() {
        OpenMeteoProvider.parseForecast("""{"latitude":1.0}""")
    }

    @Test(expected = IOException::class)
    fun malformedJsonFails() {
        OpenMeteoProvider.parseGeocoding("<html>busy</html>")
    }
}
