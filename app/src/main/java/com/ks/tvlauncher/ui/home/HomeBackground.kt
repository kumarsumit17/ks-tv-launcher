package com.ks.tvlauncher.ui.home

import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin
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
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
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
 * 2. the wallpaper, crossfading when it changes:
 *    - dynamic: the focused app's own artwork as a tiny bitmap stretched over the screen and
 *      tinted with its accent colour;
 *    - custom: a built-in wallpaper or the user's picture, darkened by [dim];
 * 3. the dark left and bottom overlays that keep text readable.
 *
 * Cost (measured on a 32-bit TV, docs/spec/smoothness.md): these full-screen layers were
 * redrawn on every frame, even while only a tile's focus animated, and that GPU fill was most
 * of each frame. So the whole stack is drawn into one cached offscreen layer that is only
 * redrawn while the wallpaper itself changes. Everything except the user's picture is smooth
 * gradients or a 32x18 px bitmap, so it is drawn at a quarter of the screen size and scaled up
 * ([SmoothLayer]), which looks the same and makes each crossfade frame about 16x cheaper. The
 * picture keeps full resolution. Custom wallpapers are never blurred.
 */
@Composable
fun HomeBackground(wallpaper: HomeWallpaper, dim: WallpaperDim, modifier: Modifier = Modifier) {
    val motion = LocalMotion.current
    val extraBlur = LocalGlassStyle.current.blurEnabled
    val dimAlpha = Wallpapers.dimAlpha(dim)
    // The last wallpaper of each kind, so the outgoing side of a switch between a picture and
    // the others keeps showing what it showed while it fades out.
    var lastSmooth by remember { mutableStateOf<HomeWallpaper>(HomeWallpaper.Plain) }
    var lastPicture by remember { mutableStateOf<HomeWallpaper.Picture?>(null) }
    SideEffect {
        if (wallpaper is HomeWallpaper.Picture) lastPicture = wallpaper else lastSmooth = wallpaper
    }
    val smooth = if (wallpaper is HomeWallpaper.Picture) lastSmooth else wallpaper
    val picture = wallpaper as? HomeWallpaper.Picture ?: lastPicture

    Crossfade(
        targetState = wallpaper is HomeWallpaper.Picture,
        animationSpec = motion.wallpaper(),
        modifier = modifier.fillMaxSize(),
        label = "wallpaperKind",
    ) { isPicture ->
        if (isPicture) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .background(BaseGradient),
            ) {
                Crossfade(picture, animationSpec = motion.wallpaper(), label = "picture") { current ->
                    if (current != null) PictureImage(current, dimAlpha)
                }
                ReadabilityOverlays()
            }
        } else {
            SmoothLayer {
                Box(Modifier.fillMaxSize().background(BaseGradient)) {
                    Crossfade(smooth, animationSpec = motion.wallpaper(), label = "wallpaper") { current ->
                        when (current) {
                            is HomeWallpaper.BuiltIn -> BuiltInWallpaper(
                                id = current.id,
                                modifier = Modifier.fillMaxSize().drawWithContent {
                                    drawContent()
                                    drawRect(Color.Black.copy(alpha = dimAlpha))
                                },
                            )
                            is HomeWallpaper.Dynamic -> {
                                val art = current.art
                                if (art == null) PlainGlow() else DynamicArt(art, extraBlur)
                            }
                            HomeWallpaper.Plain, is HomeWallpaper.Picture -> PlainGlow()
                        }
                    }
                    ReadabilityOverlays()
                }
            }
        }
    }
}

private val BaseGradient = Brush.verticalGradient(listOf(KsColors.BackgroundTop, KsColors.BackgroundBottom))

/** How much smaller [SmoothLayer] draws than the screen. */
private const val SMOOTH_SCALE = 4

/**
 * Draws [content] at 1/[SMOOTH_SCALE] of the available size into a cached offscreen layer,
 * then scales the layer up to fill. Only for smooth content (gradients, glows, the tiny
 * dynamic bitmap), where the lower resolution cannot be seen.
 */
@Composable
private fun SmoothLayer(content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .wrapContentSize(Alignment.TopStart, unbounded = true)
                .requiredSize(maxWidth / SMOOTH_SCALE, maxHeight / SMOOTH_SCALE)
                .graphicsLayer {
                    scaleX = SMOOTH_SCALE.toFloat()
                    scaleY = SMOOTH_SCALE.toFloat()
                    transformOrigin = TransformOrigin(0f, 0f)
                    compositingStrategy = CompositingStrategy.Offscreen
                },
        ) { content() }
    }
}

@Composable
private fun PictureImage(picture: HomeWallpaper.Picture, dimAlpha: Float) {
    val image = remember(picture) { picture.bitmap.asImageBitmap() }
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

/** The dark left and bottom overlays that keep text readable over any wallpaper. */
@Composable
private fun ReadabilityOverlays() {
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
            // Drawn inside SmoothLayer, so the radius is divided by its scale (48 dp on screen).
            .then(if (extraBlur) Modifier.blur((48 / SMOOTH_SCALE).dp) else Modifier)
            .drawWithContent {
                drawContent()
                drawRect(tint)
                // Darken heavily: the wallpaper should read as the app's colour,
                // not its logo, and white banners must never wash out the text.
                drawRect(Color.Black.copy(alpha = 0.62f))
            },
    )
}
