package com.ks.tvlauncher.domain

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The "Transition speed" setting: a multiplier on every animation's duration, stored as a
 * percentage. 50 (0.5×) is the default: half the design-system durations, which feels quick
 * and still smooth on a slow TV. Lower is faster. "Reduce motion" still turns animation off.
 */
object AnimationSpeed {

    /** The steps the Settings stepper moves through, fastest first. */
    val STEPS = listOf(25, 50, 75, 100, 125, 150)

    const val DEFAULT = 50

    /** A missing value is the default; any other value snaps to the nearest step. */
    fun normalise(stored: Int?): Int {
        if (stored == null) return DEFAULT
        return STEPS.minBy { abs(it - stored) }
    }

    /** One step slower (+1) or faster (-1); stays put at either end. */
    fun step(current: Int, direction: Int): Int {
        val index = STEPS.indexOf(normalise(current))
        return STEPS[(index + direction.coerceIn(-1, 1)).coerceIn(STEPS.indices)]
    }

    /** [durationMs] scaled by [percent]; at least 1 ms, so a tween never becomes a snap. */
    fun scale(durationMs: Int, percent: Int): Int =
        (durationMs * normalise(percent) / 100.0).roundToInt().coerceAtLeast(1)

    /** "0.5×", "1×", "1.25×". */
    fun label(percent: Int): String {
        val p = normalise(percent)
        val whole = p / 100
        val fraction = p % 100
        return when {
            fraction == 0 -> "$whole×"
            fraction % 10 == 0 -> "$whole.${fraction / 10}×"
            else -> "$whole.${fraction.toString().padStart(2, '0')}×"
        }
    }
}
