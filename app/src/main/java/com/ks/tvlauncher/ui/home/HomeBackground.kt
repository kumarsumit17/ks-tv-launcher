package com.ks.tvlauncher.ui.home

import android.graphics.Bitmap
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
import com.ks.tvlauncher.domain.WallpaperDim
import com.ks.tvlauncher.domain.Wallpapers
import com.ks.tvlauncher.ui.components.BuiltInWallpaper
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.LocalGlassStyle
import com.ks.tvlauncher.ui.theme.LocalMotion

/** What Home draws behind everything, from the wallpaper mode (docs/spec/custom-wallpaper.md). */
sealed interface HomeWallpaper {
    /** The navy gradient with its soft accent glow. */
    data object Plain : HomeWallpaper

    /** The focused app's artwork; null while none is loaded (drawn like [Plain]). */
    data class Dynamic(val art: Wallpaper?) : HomeWallpaper

    /** A built-in wallpaper drawn in code. */
    data class BuiltIn(val id: Int) : HomeWallpaper

    /** The user's picture, loaded once at screen size; a new load is a new object. */
    class Picture(val bitmap: Bitmap) : HomeWallpaper
}

/**
 * Home's background, back to front:
 * 1. the plain navy gradient (always there, and the fallback when there is no wallpaper);
 * 2. the wallpaper, crossfading over 400 ms when it changes:
 *    - dynamic: the focused app's own artwork as a tiny bitmap stretched over the screen and
 *      tinted with its accent colour;
 *    - custom: a built-in wallpaper or the user's picture, darkened by [dim];
 * 3. the dark left and bottom overlays that keep text readable.
 *
 * Cost: the dynamic bitmap is 32x18 px, so stretching it already gives a soft blur on every API
 * level. On API 31+ with blur enabled, one extra blur pass smooths it further; that layer
 * only re-renders when the wallpaper changes, never per frame. Custom wallpapers are never
 * blurred: the picture is drawn as it is, and built-ins are gradients.
 */
@Composable
fun HomeBackground(wallpaper: HomeWallpaper, dim: WallpaperDim, modifier: Modifier = Modifier) {
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
            val dimAlpha = Wallpapers.dimAlpha(dim)
            when (current) {
                is HomeWallpaper.BuiltIn -> BuiltInWallpaper(
                    id = current.id,
                    modifier = Modifier.fillMaxSize().drawWithContent {
                        drawContent()
                        drawRect(Color.Black.copy(alpha = dimAlpha))
                    },
                )
                is HomeWallpaper.Picture -> {
                    val image = remember(current) { current.bitmap.asImageBitmap() }
                    Image(
                        bitmap = image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        filterQuality = FilterQuality.Medium,
                        modifier = Modifier.fillMaxSize().drawWithContent {
                            drawContent()
                            drawRect(Color.Black.copy(alpha = dimAlpha))
                        },
                    )
                }
                is HomeWallpaper.Dynamic -> {
                    val art = current.art
                    if (art == null) PlainGlow() else DynamicArt(art, extraBlur)
                }
                HomeWallpaper.Plain -> PlainGlow()
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

/** Plain background: a soft accent glow so the screen is not flat. */
@Composable
private fun PlainGlow() {
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
}

@Composable
private fun DynamicArt(art: Wallpaper, extraBlur: Boolean) {
    val image = remember(art) { art.bitmap.asImageBitmap() }
    val tint = remember(art) { Color(art.tint).copy(alpha = 0.22f) }
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
