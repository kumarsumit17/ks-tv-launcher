package com.ks.tvlauncher.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ks.tvlauncher.data.apps.Wallpaper
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.LocalGlassStyle
import com.ks.tvlauncher.ui.theme.LocalMotion

/**
 * Home's background, back to front:
 * 1. the plain navy gradient (always there, and the fallback when there is no wallpaper);
 * 2. the dynamic wallpaper: the focused app's own artwork as a tiny bitmap stretched over the
 *    screen and tinted with its accent colour, crossfading over 400 ms when the app changes;
 * 3. the dark left and bottom overlays that keep text readable.
 *
 * Cost: the bitmap is 32x18 px, so stretching it already gives a soft blur on every API
 * level. On API 31+ with blur enabled, one extra blur pass smooths it further; that layer
 * only re-renders when the wallpaper changes, never per frame.
 */
@Composable
fun HomeBackground(wallpaper: Wallpaper?, modifier: Modifier = Modifier) {
    val motion = LocalMotion.current
    val extraBlur = LocalGlassStyle.current.blurEnabled

    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(KsColors.BackgroundTop, KsColors.BackgroundBottom))),
    ) {
        Crossfade(
            targetState = wallpaper,
            animationSpec = motion.wallpaper(),
            modifier = Modifier.fillMaxSize(),
            label = "wallpaper",
        ) { current ->
            if (current == null) {
                // Plain background: a soft accent glow so the screen is not flat.
                Box(
                    Modifier.fillMaxSize().drawBehind {
                        drawRect(
                            Brush.radialGradient(
                                colors = listOf(KsColors.Accent.copy(alpha = 0.16f), Color.Transparent),
                                center = Offset(size.width * 0.82f, size.height * 0.22f),
                                radius = size.width * 0.45f,
                            ),
                        )
                    },
                )
            } else {
                val image = remember(current) { current.bitmap.asImageBitmap() }
                val tint = remember(current) { Color(current.tint).copy(alpha = 0.22f) }
                Image(
                    bitmap = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.Low,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (extraBlur) Modifier.blur(48.dp) else Modifier)
                        .drawWithContent {
                            drawContent()
                            drawRect(tint)
                            // Darken heavily: the wallpaper should read as the app's colour,
                            // not its logo, and white banners must never wash out the text.
                            drawRect(Color.Black.copy(alpha = 0.62f))
                        },
                )
            }
        }

        Box(
            Modifier.fillMaxSize().drawBehind {
                drawRect(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent),
                        endX = size.width * 0.65f,
                    ),
                )
                drawRect(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                        startY = size.height * 0.5f,
                    ),
                )
            },
        )
    }
}
