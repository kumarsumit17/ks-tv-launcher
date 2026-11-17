package com.ks.tvlauncher.ui.theme

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
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
    val motion = remember(visual.reduceMotion, systemAnimations) {
        MotionPolicy(enabled = systemAnimations && !visual.reduceMotion)
    }
    val glass = remember(visual.glassIntensity) { glassStyleFor(visual.glassIntensity) }

    MaterialTheme(colorScheme = KsColorScheme, typography = KsTypography) {
        CompositionLocalProvider(
            LocalMotion provides motion,
            LocalGlassStyle provides glass,
            LocalContentColor provides KsColors.TextPrimary,
            content = content,
        )
    }
}
