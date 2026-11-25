package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodesTest {

    @Test
    fun wmoCodesMapToSixGroups() {
        assertEquals(WeatherGroup.CLEAR, WeatherCodes.group(0))
        assertEquals(WeatherGroup.CLEAR, WeatherCodes.group(1))
        assertEquals(WeatherGroup.CLOUDY, WeatherCodes.group(2))
        assertEquals(WeatherGroup.CLOUDY, WeatherCodes.group(3))
        assertEquals(WeatherGroup.FOG, WeatherCodes.group(45))
        assertEquals(WeatherGroup.FOG, WeatherCodes.group(48))
        listOf(51, 55, 61, 65, 66, 67, 80, 82).forEach { assertEquals("code $it", WeatherGroup.RAIN, WeatherCodes.group(it)) }
        listOf(71, 75, 77, 85, 86).forEach { assertEquals("code $it", WeatherGroup.SNOW, WeatherCodes.group(it)) }
        listOf(95, 96, 99).forEach { assertEquals("code $it", WeatherGroup.STORM, WeatherCodes.group(it)) }
        assertEquals(WeatherGroup.CLOUDY, WeatherCodes.group(42))
    }

    @Test
    fun temperatureRoundsAndConverts() {
        assertEquals(27, WeatherCodes.temperature(26.5, TemperatureUnit.CELSIUS))
        assertEquals(26, WeatherCodes.temperature(26.4, TemperatureUnit.CELSIUS))
        assertEquals(-3, WeatherCodes.temperature(-3.4, TemperatureUnit.CELSIUS))
        assertEquals(32, WeatherCodes.temperature(0.0, TemperatureUnit.FAHRENHEIT))
        assertEquals(81, WeatherCodes.temperature(27.0, TemperatureUnit.FAHRENHEIT))
        assertEquals(212, WeatherCodes.temperature(100.0, TemperatureUnit.FAHRENHEIT))
    }

    @Test
    fun cityDescriptionAndKey() {
        val city = WeatherCity("Pune", "Maharashtra", "India", 18.51957, 73.85535)
        assertEquals("Pune, Maharashtra, India", city.description)
        assertEquals("18.520,73.855", city.key)
        assertEquals("Singapore", WeatherCity("Singapore", "", "Singapore", 1.29, 103.85).description)
    }
}
