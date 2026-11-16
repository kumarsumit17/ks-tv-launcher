package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceNamesTest {

    @Test
    fun manufacturerIsCapitalisedAndPrefixed() {
        assertEquals("Google Chromecast", DeviceNames.tidyModel("google", "Chromecast"))
    }

    @Test
    fun manufacturerIsNotRepeated() {
        assertEquals("SONY BRAVIA 4K", DeviceNames.tidyModel("Sony", "SONY BRAVIA 4K"))
        assertEquals("Xiaomi TV Stick", DeviceNames.tidyModel("Xiaomi", "xiaomi TV Stick"))
    }

    @Test
    fun missingOrUnknownManufacturerLeavesTheModel() {
        assertEquals("Fire TV", DeviceNames.tidyModel(null, "fire TV"))
        assertEquals("Fire TV", DeviceNames.tidyModel("unknown", "Fire TV"))
    }

    @Test
    fun missingModelLeavesTheManufacturer() {
        assertEquals("Google", DeviceNames.tidyModel("google", " "))
    }

    @Test
    fun ownTvNameWinsWhenSwitchedOnAndFilledIn() {
        assertEquals("Den", DeviceNames.displayName(true, " Den ", "Living Room TV", "Google Chromecast"))
    }

    @Test
    fun systemNameIsUsedWhenOwnNameIsOffOrBlank() {
        assertEquals("Living Room TV", DeviceNames.displayName(false, "Den", "Living Room TV", "Google Chromecast"))
        assertEquals("Living Room TV", DeviceNames.displayName(true, "  ", "Living Room TV", "Google Chromecast"))
    }

    @Test
    fun modelIsTheLastFallback() {
        assertEquals("Google Chromecast", DeviceNames.displayName(false, "", null, "Google Chromecast"))
        assertEquals("Google Chromecast", DeviceNames.displayName(false, "", "  ", "Google Chromecast"))
    }
}
