package com.ks.tvlauncher.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Hand-written line icons on a 24x24 grid: 1.8 stroke, round caps and joins.
 * Each vector is built once, on first use. `material-icons-extended` is not used.
 */
object KsIcons {
    val Gear: ImageVector by lazy {
        stroked("Gear") {
            circle(12f, 12f, 3f)
            circle(12f, 12f, 7f)
            // Eight teeth around the rim.
            moveTo(12f, 2.5f); lineTo(12f, 5f)
            moveTo(12f, 19f); lineTo(12f, 21.5f)
            moveTo(2.5f, 12f); lineTo(5f, 12f)
            moveTo(19f, 12f); lineTo(21.5f, 12f)
            moveTo(5.3f, 5.3f); lineTo(7.05f, 7.05f)
            moveTo(16.95f, 16.95f); lineTo(18.7f, 18.7f)
            moveTo(5.3f, 18.7f); lineTo(7.05f, 16.95f)
            moveTo(16.95f, 7.05f); lineTo(18.7f, 5.3f)
        }
    }

    val Tv: ImageVector by lazy {
        stroked("Tv") {
            roundRect(3f, 5f, 18f, 12f, 2f)
            moveTo(8.5f, 20.5f); lineTo(15.5f, 20.5f)
            moveTo(12f, 17f); lineTo(12f, 20.5f)
        }
    }

    val Person: ImageVector by lazy {
        stroked("Person") {
            circle(12f, 8f, 3.5f)
            moveTo(5f, 20f)
            curveTo(5f, 16f, 8f, 14.5f, 12f, 14.5f)
            curveTo(16f, 14.5f, 19f, 16f, 19f, 20f)
        }
    }

    val Play: ImageVector by lazy {
        ImageVector.Builder("Play", 24.dp, 24.dp, 24f, 24f)
            .path(fill = SolidColor(Color.White)) {
                moveTo(8f, 5.5f); lineTo(18.5f, 12f); lineTo(8f, 18.5f); close()
            }
            .build()
    }

    val Cloud: ImageVector by lazy {
        stroked("Cloud") {
            moveTo(7f, 18.5f)
            curveTo(4.5f, 18.5f, 3f, 16.8f, 3f, 14.8f)
            curveTo(3f, 12.8f, 4.5f, 11.2f, 6.6f, 11.1f)
            curveTo(7.2f, 8.4f, 9.4f, 6.5f, 12.2f, 6.5f)
            curveTo(15.3f, 6.5f, 17.8f, 8.9f, 18f, 11.9f)
            curveTo(19.8f, 12.2f, 21f, 13.6f, 21f, 15.3f)
            curveTo(21f, 17.1f, 19.6f, 18.5f, 17.8f, 18.5f)
            close()
        }
    }

    val Sun: ImageVector by lazy {
        stroked("Sun") {
            circle(12f, 12f, 4f)
            moveTo(12f, 2.5f); lineTo(12f, 4.5f)
            moveTo(12f, 19.5f); lineTo(12f, 21.5f)
            moveTo(2.5f, 12f); lineTo(4.5f, 12f)
            moveTo(19.5f, 12f); lineTo(21.5f, 12f)
            moveTo(5.3f, 5.3f); lineTo(6.7f, 6.7f)
            moveTo(17.3f, 17.3f); lineTo(18.7f, 18.7f)
            moveTo(5.3f, 18.7f); lineTo(6.7f, 17.3f)
            moveTo(17.3f, 6.7f); lineTo(18.7f, 5.3f)
        }
    }

    val Grid: ImageVector by lazy {
        stroked("Grid") {
            roundRect(4f, 4f, 6.5f, 6.5f, 1.5f)
            roundRect(13.5f, 4f, 6.5f, 6.5f, 1.5f)
            roundRect(4f, 13.5f, 6.5f, 6.5f, 1.5f)
            roundRect(13.5f, 13.5f, 6.5f, 6.5f, 1.5f)
        }
    }

    val Sliders: ImageVector by lazy {
        stroked("Sliders") {
            moveTo(4f, 8f); lineTo(13f, 8f)
            circle(15.5f, 8f, 2.5f)
            moveTo(18f, 8f); lineTo(20f, 8f)
            moveTo(4f, 16f); lineTo(6f, 16f)
            circle(8.5f, 16f, 2.5f)
            moveTo(11f, 16f); lineTo(20f, 16f)
        }
    }

    val Plus: ImageVector by lazy {
        stroked("Plus") {
            moveTo(12f, 5f); lineTo(12f, 19f)
            moveTo(5f, 12f); lineTo(19f, 12f)
        }
    }

    val Minus: ImageVector by lazy {
        stroked("Minus") { moveTo(5f, 12f); lineTo(19f, 12f) }
    }

    val ChevronLeft: ImageVector by lazy {
        stroked("ChevronLeft") { moveTo(14.5f, 6f); lineTo(8.5f, 12f); lineTo(14.5f, 18f) }
    }

    val ChevronRight: ImageVector by lazy {
        stroked("ChevronRight") { moveTo(9.5f, 6f); lineTo(15.5f, 12f); lineTo(9.5f, 18f) }
    }

    val Check: ImageVector by lazy {
        stroked("Check") { moveTo(5f, 12.5f); lineTo(10f, 17.5f); lineTo(19f, 7f) }
    }

    val Search: ImageVector by lazy {
        stroked("Search") {
            circle(11f, 11f, 6f)
            moveTo(15.5f, 15.5f); lineTo(20f, 20f)
        }
    }

    val Close: ImageVector by lazy {
        stroked("Close") {
            moveTo(6f, 6f); lineTo(18f, 18f)
            moveTo(18f, 6f); lineTo(6f, 18f)
        }
    }

    val Wifi: ImageVector by lazy {
        stroked("Wifi") {
            moveTo(3f, 9.5f); curveTo(8f, 4.8f, 16f, 4.8f, 21f, 9.5f)
            moveTo(6f, 12.8f); curveTo(9.4f, 9.8f, 14.6f, 9.8f, 18f, 12.8f)
            moveTo(9f, 16f); curveTo(10.8f, 14.6f, 13.2f, 14.6f, 15f, 16f)
            circle(12f, 19f, 0.6f)
        }
    }

    val Bluetooth: ImageVector by lazy {
        stroked("Bluetooth") {
            moveTo(7f, 7.5f); lineTo(16.5f, 16f); lineTo(12f, 20f); lineTo(12f, 4f); lineTo(16.5f, 8f); lineTo(7f, 16.5f)
        }
    }

    val Monitor: ImageVector by lazy {
        stroked("Monitor") {
            roundRect(3f, 4.5f, 18f, 12f, 2f)
            moveTo(9f, 20f); lineTo(15f, 20f)
            moveTo(12f, 16.5f); lineTo(12f, 20f)
        }
    }

    /** A mains-style plug: Control Center Inputs, so it never looks like Display's monitor. */
    val Plug: ImageVector by lazy {
        stroked("Plug") {
            moveTo(9f, 3f); lineTo(9f, 7f)
            moveTo(15f, 3f); lineTo(15f, 7f)
            moveTo(6f, 7f); lineTo(18f, 7f); lineTo(18f, 11f)
            curveTo(18f, 14f, 15.5f, 16f, 12f, 16f)
            curveTo(8.5f, 16f, 6f, 14f, 6f, 11f)
            close()
            moveTo(12f, 16f); lineTo(12f, 21f)
        }
    }

    val Moon: ImageVector by lazy {
        stroked("Moon") {
            moveTo(19.5f, 14.5f)
            curveTo(15f, 16f, 9f, 12f, 10.5f, 4.5f)
            curveTo(6.5f, 5.5f, 4f, 9f, 4f, 12.5f)
            curveTo(4f, 17f, 7.5f, 20f, 12f, 20f)
            curveTo(15.3f, 20f, 18.2f, 17.8f, 19.5f, 14.5f)
            close()
        }
    }

    val Fog: ImageVector by lazy {
        stroked("Fog") {
            moveTo(4f, 9f); lineTo(20f, 9f)
            moveTo(6f, 13f); lineTo(18f, 13f)
            moveTo(4f, 17f); lineTo(20f, 17f)
        }
    }

    val Rain: ImageVector by lazy {
        stroked("Rain") {
            moveTo(7f, 14.5f)
            curveTo(4.8f, 14.5f, 3.5f, 13f, 3.5f, 11.3f)
            curveTo(3.5f, 9.5f, 4.8f, 8.2f, 6.6f, 8.1f)
            curveTo(7.2f, 5.6f, 9.4f, 4f, 12f, 4f)
            curveTo(15f, 4f, 17.3f, 6.2f, 17.5f, 9f)
            curveTo(19.2f, 9.3f, 20.5f, 10.6f, 20.5f, 12f)
            curveTo(20.5f, 13.5f, 19.2f, 14.5f, 17.5f, 14.5f)
            close()
            moveTo(8f, 17.5f); lineTo(7f, 20f)
            moveTo(12.5f, 17.5f); lineTo(11.5f, 20f)
            moveTo(17f, 17.5f); lineTo(16f, 20f)
        }
    }

    val Snow: ImageVector by lazy {
        stroked("Snow") {
            moveTo(12f, 3.5f); lineTo(12f, 20.5f)
            moveTo(4.6f, 7.75f); lineTo(19.4f, 16.25f)
            moveTo(4.6f, 16.25f); lineTo(19.4f, 7.75f)
            moveTo(9.5f, 4.5f); lineTo(12f, 6.5f); lineTo(14.5f, 4.5f)
            moveTo(9.5f, 19.5f); lineTo(12f, 17.5f); lineTo(14.5f, 19.5f)
        }
    }

    val Storm: ImageVector by lazy {
        stroked("Storm") {
            moveTo(13f, 3.5f); lineTo(6.5f, 13f); lineTo(11.5f, 13f); lineTo(10.5f, 20.5f); lineTo(17.5f, 10.5f); lineTo(12.5f, 10.5f)
            close()
        }
    }

    val Memory: ImageVector by lazy {
        stroked("Memory") {
            roundRect(6f, 6f, 12f, 12f, 2f)
            roundRect(9.5f, 9.5f, 5f, 5f, 1f)
            moveTo(9f, 3f); lineTo(9f, 6f); moveTo(15f, 3f); lineTo(15f, 6f)
            moveTo(9f, 18f); lineTo(9f, 21f); moveTo(15f, 18f); lineTo(15f, 21f)
            moveTo(3f, 9f); lineTo(6f, 9f); moveTo(3f, 15f); lineTo(6f, 15f)
            moveTo(18f, 9f); lineTo(21f, 9f); moveTo(18f, 15f); lineTo(21f, 15f)
        }
    }

    val ArrowUpRight: ImageVector by lazy {
        stroked("ArrowUpRight") {
            moveTo(7f, 17f); lineTo(17f, 7f)
            moveTo(9f, 7f); lineTo(17f, 7f); lineTo(17f, 15f)
        }
    }
}

private fun stroked(name: String, block: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        .path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block,
        )
        .build()

private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx + r, cy)
    arcToRelative(r, r, 0f, true, true, -2 * r, 0f)
    arcToRelative(r, r, 0f, true, true, 2 * r, 0f)
}

private fun PathBuilder.roundRect(x: Float, y: Float, w: Float, h: Float, r: Float) {
    moveTo(x + r, y)
    lineTo(x + w - r, y)
    arcToRelative(r, r, 0f, false, true, r, r)
    lineTo(x + w, y + h - r)
    arcToRelative(r, r, 0f, false, true, -r, r)
    lineTo(x + r, y + h)
    arcToRelative(r, r, 0f, false, true, -r, -r)
    lineTo(x, y + r)
    arcToRelative(r, r, 0f, false, true, r, -r)
    close()
}
