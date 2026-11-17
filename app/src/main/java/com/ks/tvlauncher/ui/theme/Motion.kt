package com.ks.tvlauncher.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import com.ks.tvlauncher.domain.AnimationSpeed

object KsMotion {
    const val FOCUS_MS = 200
    const val WALLPAPER_MS = 400
    const val PANEL_MS = 280
}

/**
 * Decides whether anything animates, and how long it takes. When disabled, every spec snaps,
 * so no animation frames are produced at all. Disabled by the "Reduce motion" setting or when
 * the system animator scale is 0. [speedPercent] is the "Transition speed" setting: every
 * duration is scaled by it (50 = half as long, the default).
 */
@Immutable
data class MotionPolicy(val enabled: Boolean = true, val speedPercent: Int = AnimationSpeed.DEFAULT) {
    fun <T> focus(): FiniteAnimationSpec<T> = spec(KsMotion.FOCUS_MS)
    fun <T> wallpaper(): FiniteAnimationSpec<T> = spec(KsMotion.WALLPAPER_MS)
    fun <T> panel(): FiniteAnimationSpec<T> = spec(KsMotion.PANEL_MS)

    private fun <T> spec(durationMs: Int): FiniteAnimationSpec<T> =
        if (enabled) tween(AnimationSpeed.scale(durationMs, speedPercent), easing = FastOutSlowInEasing) else snap()
}

/**
 * Not a static local: changing Reduce motion or the speed then recomposes only the code that
 * reads motion, instead of the whole screen. A whole-screen recomposition froze the TV for a
 * moment, so a second OK press on the toggle flipped it straight back.
 */
val LocalMotion = compositionLocalOf { MotionPolicy() }

/** False when the user or a developer option has set the system animator scale to 0. */
fun systemAnimationsEnabled(context: Context): Boolean =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
