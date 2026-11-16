package com.ks.tvlauncher.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object KsShapes {
    val Tile: Shape = RoundedCornerShape(11.dp)
    val Card: Shape = RoundedCornerShape(12.dp)
    val Panel: Shape = RoundedCornerShape(16.dp)
    val Badge: Shape = RoundedCornerShape(7.dp)
    val Pill: Shape = CircleShape
}

/** Layout constants for the 960x540 dp design canvas. */
object KsDimens {
    /** Overscan-safe padding. */
    val OverscanHorizontal = 48.dp
    val OverscanVertical = 27.dp

    val TileWidth = 160.dp
    val TileHeight = 90.dp
    val TileGap = 16.dp
}
