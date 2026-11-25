package com.ks.tvlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.LocalGlassStyle

/**
 * The shared glass surface: a translucent fill with a 1dp hairline border, clipped to [shape].
 *
 * [frosted] additionally blurs whatever is behind the surface. It only has an effect on
 * API 31+, at Medium/High glass intensity, and inside a [FrostSource]. Use it for surfaces
 * that stay still; never for tiles inside a scrolling row.
 */
@Composable
fun Modifier.glass(
    shape: Shape = KsShapes.Card,
    frosted: Boolean = false,
    fill: Color? = null,
): Modifier {
    val style = LocalGlassStyle.current
    val fillColor = fill ?: Color.White.copy(alpha = style.fillAlpha)
    return this
        .clip(shape)
        .frost(enabled = frosted)
        .background(fillColor)
        .border(1.dp, KsColors.GlassBorder, shape)
}
