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
