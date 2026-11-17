package com.ks.tvlauncher.ui.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ks.tvlauncher.R
import com.ks.tvlauncher.domain.UsageLevel
import com.ks.tvlauncher.ui.components.PillTone
import com.ks.tvlauncher.ui.theme.KsColors

/** Gradient colours for a level: blue under 75 %, amber to 90 %, red above. */
fun usageColors(level: UsageLevel): List<Color> = when (level) {
    UsageLevel.NORMAL -> listOf(KsColors.UsageNormalStart, KsColors.UsageNormalEnd)
    UsageLevel.GETTING_FULL -> listOf(KsColors.UsageFullStart, KsColors.UsageFullEnd)
    UsageLevel.ALMOST_FULL -> listOf(KsColors.UsageCritical, KsColors.UsageCritical)
}

fun usageTone(level: UsageLevel): PillTone = when (level) {
    UsageLevel.NORMAL -> PillTone.Neutral
    UsageLevel.GETTING_FULL -> PillTone.Warning
    UsageLevel.ALMOST_FULL -> PillTone.Danger
}

@Composable
fun usageTag(level: UsageLevel): String = stringResource(
    when (level) {
        UsageLevel.NORMAL -> R.string.mem_normal
        UsageLevel.GETTING_FULL -> R.string.mem_getting_full
        UsageLevel.ALMOST_FULL -> R.string.mem_almost_full
    },
)

/** Thin rounded bar; [fraction] is 0–1. Drawn in one canvas, no layout per frame. */
@Composable
fun UsageBar(fraction: Float, level: UsageLevel, modifier: Modifier = Modifier) {
    val colors = usageColors(level)
    Canvas(modifier) {
        val radius = CornerRadius(size.height / 2f, size.height / 2f)
        drawRoundRect(KsColors.UsageTrack, cornerRadius = radius)
        val width = size.width * fraction.coerceIn(0f, 1f)
        if (width > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(colors, startX = 0f, endX = size.width),
                size = Size(width, size.height),
                cornerRadius = radius,
            )
        }
    }
}

/** Ring gauge for the panel; the arc starts at 12 o'clock. */
@Composable
fun UsageRing(fraction: Float, level: UsageLevel, modifier: Modifier = Modifier, stroke: Dp = 12.dp) {
    val colors = usageColors(level)
    Canvas(modifier) {
        val width = stroke.toPx()
        val inset = width / 2f
        val arcSize = Size(size.width - width, size.height - width)
        val topLeft = Offset(inset, inset)
        drawArc(KsColors.UsageTrack, 0f, 360f, false, topLeft, arcSize, style = Stroke(width))
        drawArc(
            brush = Brush.linearGradient(colors),
            startAngle = -90f,
            sweepAngle = 360f * fraction.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width, cap = StrokeCap.Round),
        )
    }
}
