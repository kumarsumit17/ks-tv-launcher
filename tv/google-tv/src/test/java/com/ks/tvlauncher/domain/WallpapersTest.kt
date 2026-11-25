package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpapersTest {

    @Test
    fun storedModeWins() {
        assertEquals(WallpaperMode.CUSTOM, Wallpapers.mode("CUSTOM", legacyDynamic = false))
        assertEquals(WallpaperMode.DYNAMIC, Wallpapers.mode("DYNAMIC", legacyDynamic = false))
    }

    @Test
    fun oldSwitchOffMigratesToPlain() {
        assertEquals(WallpaperMode.PLAIN, Wallpapers.mode(null, legacyDynamic = false))
        assertEquals(WallpaperMode.DYNAMIC, Wallpapers.mode(null, legacyDynamic = true))
        assertEquals(WallpaperMode.DYNAMIC, Wallpapers.mode(null, legacyDynamic = null))
    }

    @Test
    fun unknownModeFollowsTheOldSwitch() {
        assertEquals(WallpaperMode.PLAIN, Wallpapers.mode("SPARKLY", legacyDynamic = false))
        assertEquals(WallpaperMode.DYNAMIC, Wallpapers.mode("", legacyDynamic = null))
    }

    @Test
    fun dimDefaultsToMedium() {
        assertEquals(WallpaperDim.MEDIUM, Wallpapers.dim(null))
        assertEquals(WallpaperDim.MEDIUM, Wallpapers.dim("bogus"))
        assertEquals(WallpaperDim.HIGH, Wallpapers.dim("HIGH"))
    }

    @Test
    fun dimLevelsGetDarker() {
        val alphas = WallpaperDim.entries.map(Wallpapers::dimAlpha)
        assertEquals(alphas.sorted(), alphas)
        assertTrue(alphas.all { it in 0f..1f })
    }

    @Test
    fun imageRoundTrips() {
        listOf(WallpaperImage.BuiltIn(0), WallpaperImage.BuiltIn(5), WallpaperImage.Picture(1_731_830_000_000L)).forEach {
            assertEquals(it, Wallpapers.decode(Wallpapers.encode(it)))
        }
    }

    @Test
    fun damagedImageReadsAsFirstBuiltIn() {
        listOf(null, "", "builtin:", "builtin:6", "builtin:-1", "picture:abc", "photo:1", "builtin").forEach {
            assertEquals(it, WallpaperImage.BuiltIn(0), Wallpapers.decode(it))
        }
    }

    @Test
    fun nextCyclesThroughAllModes() {
        assertEquals(WallpaperMode.CUSTOM, Wallpapers.next(WallpaperMode.DYNAMIC))
        assertEquals(WallpaperMode.PLAIN, Wallpapers.next(WallpaperMode.CUSTOM))
        assertEquals(WallpaperMode.DYNAMIC, Wallpapers.next(WallpaperMode.PLAIN))
    }

    @Test
    fun smallImagesAreNotScaledUp() {
        assertEquals(1280 to 720, Wallpapers.fitWithin(1280, 720))
        assertEquals(1920 to 1080, Wallpapers.fitWithin(1920, 1080))
    }

    @Test
    fun largeImagesFitInsideFullHd() {
        assertEquals(1920 to 1080, Wallpapers.fitWithin(3840, 2160))
        // Portrait: height is the limit.
        assertEquals(810 to 1080, Wallpapers.fitWithin(3024, 4032))
        // Very wide: width is the limit.
        assertEquals(1920 to 480, Wallpapers.fitWithin(8000, 2000))
    }

    @Test
    fun extremeShapesNeverGiveZero() {
        val (w, h) = Wallpapers.fitWithin(100_000, 10)
        assertEquals(1920, w)
        assertEquals(1, h)
    }
}
