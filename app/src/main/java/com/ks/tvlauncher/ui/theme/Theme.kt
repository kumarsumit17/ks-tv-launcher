package com.ks.tvlauncher.ui.theme

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import com.ks.tvlauncher.data.settings.GlassIntensity

/** The user's visual-effect choices that the design system reacts to. */
@Immutable
data class VisualSettings(
    val reduceMotion: Boolean = false,
    /** Percent of the design-system durations ([com.ks.tvlauncher.domain.AnimationSpeed]). */
    val animationSpeed: Int = com.ks.tvlauncher.domain.AnimationSpeed.DEFAULT,
    val glassIntensity: GlassIntensity = GlassIntensity.MEDIUM,
)

/**
 * How glass surfaces render. Real blur needs API 31+ and is the most expensive effect in
 * the app, so it is opt-in per surface (`frosted = true`), limited to surfaces that do not
 * scroll, and switched off entirely at [GlassIntensity.LOW].
 */
@Immutable
data class GlassStyle(val blurRadius: Dp, val fillAlpha: Float) {
    val blurEnabled: Boolean get() = blurRadius > 0.dp
}

val LocalGlassStyle = staticCompositionLocalOf { GlassStyle(blurRadius = 0.dp, fillAlpha = 0.12f) }

private fun glassStyleFor(intensity: GlassIntensity): GlassStyle {
    val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    return when (intensity) {
        // No blur: a slightly denser fill keeps text readable without it.
        GlassIntensity.LOW -> GlassStyle(blurRadius = 0.dp, fillAlpha = 0.16f)
        GlassIntensity.MEDIUM -> GlassStyle(blurRadius = if (canBlur) 16.dp else 0.dp, fillAlpha = 0.12f)
        GlassIntensity.HIGH -> GlassStyle(blurRadius = if (canBlur) 28.dp else 0.dp, fillAlpha = 0.12f)
    }
}

/**
 * The design canvas (specs/design-system.md, "Screen sizes"): every screen is laid out for
 * 960x540 dp, Google TV's canvas, tuned for 55 and 65 inch TVs. A TV does not tell apps its
 * size in inches, and makers ship different densities (1080p at 213, 240 or 320; the user's
 * Display size setting changes it too), so the canvas is scaled to fill the window exactly
 * instead of trusting the system density. On a standard Google TV the scale is 1.
 */
object KsCanvas {
    const val WIDTH_DP = 960f
    const val HEIGHT_DP = 540f

    /** The largest text scale honoured: Android TV's "Largest". Beyond it, cards would clip. */
    const val MAX_FONT_SCALE = 1.3f

    /**
     * The share of a larger text scale given back to the layout as extra room: at 1.3x text
     * the canvas grows 7.5 %, so Home's rows still fit 540 dp of screen height, and text is
     * still about 21 % larger on screen than at the normal size.
     */
    const val ROOM_FOR_LARGE_TEXT = 0.25f

    /**
     * Pixels per dp that make the 960x540 canvas fill a [widthPx] x [heightPx] window (the
     * whole of one side, centred content on the other for a non-16:9 window); null before
     * the window has a size.
     */
    fun pixelsPerDp(widthPx: Int, heightPx: Int, fontScale: Float = 1f): Float? =
        if (widthPx <= 0 || heightPx <= 0) {
            null
        } else {
            minOf(widthPx / WIDTH_DP, heightPx / HEIGHT_DP) / roomFor(fontScale)
        }

    /** How much larger the canvas is in dp for [fontScale] (1 at normal or smaller text). */
    fun roomFor(fontScale: Float): Float = 1f + (fontScale(fontScale) - 1f).coerceAtLeast(0f) * ROOM_FOR_LARGE_TEXT

    /** The system's text scale, at most [MAX_FONT_SCALE]. */
    fun fontScale(system: Float): Float = system.coerceAtMost(MAX_FONT_SCALE)
}

private val KsColorScheme = darkColorScheme(
    primary = KsColors.PrimaryButton,
    onPrimary = KsColors.OnPrimaryButton,
    background = KsColors.BackgroundBottom,
    onBackground = KsColors.TextPrimary,
    surface = KsColors.BackgroundTop,
    onSurface = KsColors.TextPrimary,
    onSurfaceVariant = KsColors.TextSecondary,
)

@Composable
fun KsTheme(
    visual: VisualSettings = VisualSettings(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemAnimations = remember(context) { systemAnimationsEnabled(context) }
    val motion = remember(visual.reduceMotion, visual.animationSpeed, systemAnimations) {
        MotionPolicy(enabled = systemAnimations && !visual.reduceMotion, speedPercent = visual.animationSpeed)
    }
    val glass = remember(visual.glassIntensity) { glassStyleFor(visual.glassIntensity) }
    val window = LocalWindowInfo.current.containerSize
    val system = LocalDensity.current
    val canvas = remember(window, system) {
        KsCanvas.pixelsPerDp(window.width, window.height, system.fontScale)
            ?.let { Density(density = it, fontScale = KsCanvas.fontScale(system.fontScale)) }
            ?: system
    }

    MaterialTheme(colorScheme = KsColorScheme, typography = KsTypography) {
        CompositionLocalProvider(
            LocalDensity provides canvas,
            LocalMotion provides motion,
            LocalGlassStyle provides glass,
            LocalContentColor provides KsColors.TextPrimary,
            content = content,
        )
    }
}
