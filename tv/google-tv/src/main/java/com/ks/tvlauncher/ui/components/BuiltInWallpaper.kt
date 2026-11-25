package com.ks.tvlauncher.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.ks.tvlauncher.R
import com.ks.tvlauncher.domain.Wallpapers
import com.ks.tvlauncher.ui.theme.KsColors

/** A soft glow: [color] at [alpha], centred at a fraction of the size, radius a fraction of the width. */
private class Glow(val color: Color, val alpha: Float, val x: Float, val y: Float, val radius: Float)

private class Design(@StringRes val name: Int, val top: Color, val bottom: Color, val diagonal: Boolean, val glows: List<Glow>)

/**
 * The six built-in wallpapers: gradients and soft glows in the design-system palette, drawn in
 * code. No photos, downloads or third-party art. Index = [com.ks.tvlauncher.domain.WallpaperImage.BuiltIn.id].
 */
private val Designs = listOf(
    Design(
        R.string.wallpaper_midnight, KsColors.BackgroundTop, KsColors.BackgroundBottom, false,
        listOf(Glow(KsColors.Accent, 0.30f, 0.80f, 0.20f, 0.55f), Glow(Color(0xFF3B4FA8), 0.30f, 0.15f, 0.85f, 0.50f)),
    ),
    Design(
        R.string.wallpaper_aurora, Color(0xFF071A22), Color(0xFF04080D), false,
        listOf(
            Glow(KsColors.Success, 0.20f, 0.25f, 0.25f, 0.50f),
            Glow(KsColors.Accent, 0.22f, 0.75f, 0.55f, 0.50f),
            Glow(Color(0xFF2BB3A8), 0.20f, 0.55f, 0.10f, 0.40f),
        ),
    ),
    Design(
        R.string.wallpaper_dusk, Color(0xFF1A1030), Color(0xFF07050D), false,
        listOf(Glow(KsColors.Danger, 0.20f, 0.80f, 0.75f, 0.55f), Glow(Color(0xFF8A5CF6), 0.28f, 0.20f, 0.30f, 0.55f)),
    ),
    Design(
        R.string.wallpaper_ember, Color(0xFF1C0F08), Color(0xFF070504), false,
        listOf(Glow(Color(0xFFFFC46E), 0.24f, 0.85f, 0.85f, 0.60f), Glow(Color(0xFFFF7A45), 0.16f, 0.30f, 0.20f, 0.45f)),
    ),
    Design(
        R.string.wallpaper_ocean, Color(0xFF061A2E), Color(0xFF03070D), true,
        listOf(Glow(KsColors.Accent, 0.26f, 0.50f, 1.00f, 0.70f), Glow(Color(0xFF1FA2FF), 0.18f, 0.10f, 0.10f, 0.40f)),
    ),
    Design(
        R.string.wallpaper_mist, Color(0xFF1A1F2B), Color(0xFF0A0C12), false,
        listOf(Glow(Color.White, 0.10f, 0.70f, 0.30f, 0.60f), Glow(KsColors.Accent, 0.12f, 0.25f, 0.75f, 0.50f)),
    ),
).also { check(it.size == Wallpapers.BUILT_IN_COUNT) }

/** The display name of built-in wallpaper [id]. */
@StringRes
fun builtInWallpaperName(id: Int): Int = Designs[id.coerceIn(Designs.indices)].name

/**
 * Draws built-in wallpaper [id] to fill its size. Only gradients: it redraws on a size change,
 * never per frame, so it costs nothing behind Home and works as a small swatch in Settings.
 */
@Composable
fun BuiltInWallpaper(id: Int, modifier: Modifier = Modifier) {
    val design = Designs[id.coerceIn(Designs.indices)]
    Box(modifier.drawBehind { draw(design) })
}

private fun DrawScope.draw(design: Design) {
    val colors = listOf(design.top, design.bottom)
    drawRect(
        if (design.diagonal) {
            Brush.linearGradient(colors, start = Offset.Zero, end = Offset(size.width, size.height))
        } else {
            Brush.verticalGradient(colors)
        },
    )
    design.glows.forEach { glow ->
        drawRect(
            Brush.radialGradient(
                colors = listOf(glow.color.copy(alpha = glow.alpha), Color.Transparent),
                center = Offset(size.width * glow.x, size.height * glow.y),
                radius = (size.width * glow.radius).coerceAtLeast(1f),
            ),
        )
    }
}
