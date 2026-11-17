package com.ks.tvlauncher.data.weather

import com.ks.tvlauncher.domain.WeatherCity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class IpWhoIsLocatorTest {

    @Test
    fun readsCityAndCoordinates() {
        val body = """
            {"ip":"203.0.113.7","success":true,"type":"IPv4","continent":"Asia","country":"India","country_code":"IN",
             "region":"Maharashtra","city":"Pune","latitude":18.5204303,"longitude":73.8567437,"timezone":{"id":"Asia/Kolkata"}}
        """.trimIndent()
        assertEquals(WeatherCity("Pune", "Maharashtra", "India", 18.5204303, 73.8567437), IpWhoIsLocator.parse(body))
    }

    @Test(expected = IOException::class)
    fun unsuccessfulLookupFails() {
        IpWhoIsLocator.parse("""{"success":false,"message":"Reserved range"}""")
    }

    @Test(expected = IOException::class)
    fun missingCoordinatesFail() {
        IpWhoIsLocator.parse("""{"success":true,"city":"Pune"}""")
    }

    @Test(expected = IOException::class)
    fun blankCityFails() {
        IpWhoIsLocator.parse("""{"success":true,"city":"  ","latitude":1.0,"longitude":2.0}""")
    }

    @Test(expected = IOException::class)
    fun notJsonFails() {
        IpWhoIsLocator.parse("<html>rate limited</html>")
    }
}
