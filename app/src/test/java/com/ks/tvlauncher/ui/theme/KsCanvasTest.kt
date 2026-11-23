package com.ks.tvlauncher.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KsCanvasTest {
    private fun dpSize(w: Int, h: Int): Pair<Float, Float> {
        val d = KsCanvas.pixelsPerDp(w, h)!!
        return w / d to h / d
    }

    @Test
    fun standardGoogleTvDensitiesKeepTheCanvas() {
        // 720p at 213 (tvdpi), 1080p at 320 (xhdpi), 4K at 640 (xxxhdpi): the scale the system uses.
        assertEquals(1280 / 960f, KsCanvas.pixelsPerDp(1280, 720)!!, 0.001f)
        assertEquals(2f, KsCanvas.pixelsPerDp(1920, 1080)!!, 0.001f)
        assertEquals(4f, KsCanvas.pixelsPerDp(3840, 2160)!!, 0.001f)
    }

    @Test
    fun everyResolutionLaysOutTheSameCanvas() {
        for ((w, h) in listOf(1280 to 720, 1366 to 768, 1920 to 1080, 2560 to 1440, 3840 to 2160, 7680 to 4320)) {
            val (dw, dh) = dpSize(w, h)
            assertEquals("$w x $h", 960f, dw, 1f)
            assertEquals("$w x $h", 540f, dh, 1f)
        }
    }

    @Test
    fun aNonWideWindowFillsItsWidthAndKeepsTheRest() {
        // 4:3 (an old panel or a letterboxed window): full width, more height than the canvas.
        val (dw, dh) = dpSize(1440, 1080)
        assertEquals(960f, dw, 0.5f)
        assertEquals(720f, dh, 0.5f)
        // Wider than 16:9: full height, more width.
        val (ww, wh) = dpSize(2560, 1080)
        assertEquals(540f, wh, 0.5f)
        assertEquals(1280f, ww, 0.5f)
    }

    @Test
    fun noSizeYetMeansNoOverride() {
        assertNull(KsCanvas.pixelsPerDp(0, 1080))
        assertNull(KsCanvas.pixelsPerDp(1920, 0))
    }

    @Test
    fun largerTextGetsMoreRoomAndStillGrows() {
        // Normal and smaller text: the canvas is exactly 960x540.
        assertEquals(1f, KsCanvas.roomFor(1f), 0f)
        assertEquals(1f, KsCanvas.roomFor(0.85f), 0f)
        // "Largest" (1.3x): 7.5 % more room, so 580 dp of height instead of 540.
        assertEquals(1.075f, KsCanvas.roomFor(1.3f), 0.0001f)
        assertEquals(540f * 1.075f, 2160 / KsCanvas.pixelsPerDp(3840, 2160, 1.3f)!!, 0.5f)
        // Text on screen: 1.3x in dp on a canvas 1.075x larger, so about 1.21x the normal size.
        assertEquals(1.21f, 1.3f / KsCanvas.roomFor(1.3f), 0.01f)
        // Beyond the cap, nothing grows further.
        assertEquals(KsCanvas.roomFor(1.3f), KsCanvas.roomFor(2f), 0f)
    }

    @Test
    fun textScaleIsCappedAtLargest() {
        assertEquals(1f, KsCanvas.fontScale(1f), 0f)
        assertEquals(0.85f, KsCanvas.fontScale(0.85f), 0f)
        assertEquals(1.3f, KsCanvas.fontScale(1.3f), 0f)
        assertEquals(1.3f, KsCanvas.fontScale(2f), 0f)
    }
}
