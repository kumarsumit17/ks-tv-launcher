package com.ks.tvlauncher.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

object KsMotion {
    const val FOCUS_MS = 200
    const val WALLPAPER_MS = 400
    const val PANEL_MS = 280
}

/**
 * Decides whether anything animates. When disabled, every spec snaps, so no animation
 * frames are produced at all. Disabled by the "Reduce motion" setting or when the system
 * animator scale is 0.
 */
@Immutable
data class MotionPolicy(val enabled: Boolean = true) {
    fun <T> focus(): AnimationSpec<T> = spec(KsMotion.FOCUS_MS)
    fun <T> wallpaper(): AnimationSpec<T> = spec(KsMotion.WALLPAPER_MS)
    fun <T> panel(): AnimationSpec<T> = spec(KsMotion.PANEL_MS)

    private fun <T> spec(durationMs: Int): AnimationSpec<T> =
        if (enabled) tween(durationMs, easing = FastOutSlowInEasing) else snap()
}

val LocalMotion = staticCompositionLocalOf { MotionPolicy() }

/** False when the user or a developer option has set the system animator scale to 0. */
fun systemAnimationsEnabled(context: Context): Boolean =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
