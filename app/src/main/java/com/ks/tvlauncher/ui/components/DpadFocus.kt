package com.ks.tvlauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.LocalMotion

private const val FOCUSED_SCALE = 1.06f
private const val BORDER_ALPHA = 0.95f

/**
 * The launcher's single focus treatment. When the focus target that follows this modifier
 * gains D-pad focus it scales to 1.06, shows a 3dp white border, a raised shadow and a soft
 * glow in [glow]; when focus leaves, everything animates back.
 *
 * Performance: one float animation drives all four effects, and its value is only read
 * inside the graphics-layer and draw lambdas. A focus change therefore recomposes once
 * (to flip the target value and z-order); the animation frames themselves only redraw.
 * With motion disabled the spec snaps, so no animation frames run at all.
 *
 * Place it before the focusable/clickable modifier, and give lazy rows enough content
 * padding for the scaled tile and its glow.
 */
@Composable
fun Modifier.animateDpadFocus(
    glow: Color = Color.White,
    shape: Shape = KsShapes.Card,
    focusedScale: Float = FOCUSED_SCALE,
): Modifier {
    var focused by remember { mutableStateOf(false) }
    val progress = animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = LocalMotion.current.focus(),
        label = "dpadFocus",
    )

    return this
        // Focused items draw above their neighbours so the scale-up is never covered.
        .zIndex(if (focused) 1f else 0f)
        .onFocusChanged { focused = it.isFocused }
        .graphicsLayer {
            val scale = 1f + (focusedScale - 1f) * progress.value
            scaleX = scale
            scaleY = scale
        }
        .drawWithContent {
            val p = progress.value
            if (p == 0f) {
                drawContent()
                return@drawWithContent
            }
            val outline = shape.createOutline(size, layoutDirection, this)

            // Raised shadow: a dark halo offset downwards.
            translate(top = 5.dp.toPx()) {
                drawOutline(outline, Color.Black.copy(alpha = 0.28f * p), style = Stroke(12.dp.toPx()))
            }
            // Soft glow: three widening, fading rings in the glow colour.
            drawOutline(outline, glow.copy(alpha = 0.07f * p), style = Stroke(20.dp.toPx()))
            drawOutline(outline, glow.copy(alpha = 0.11f * p), style = Stroke(12.dp.toPx()))
            drawOutline(outline, glow.copy(alpha = 0.16f * p), style = Stroke(6.dp.toPx()))

            drawContent()

            drawOutline(outline, Color.White.copy(alpha = BORDER_ALPHA * p), style = Stroke(3.dp.toPx()))
        }
}

/**
 * A D-pad focus stop with the standard focus treatment. OK (or Enter) triggers [onClick];
 * holding OK triggers [onLongClick]. With no [onClick] it is focusable but not clickable.
 */
@Composable
fun DpadFocusable(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = KsShapes.Card,
    glow: Color = Color.White,
    focusedScale: Float = FOCUSED_SCALE,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = if (onClick != null) {
        Modifier.combinedClickable(
            interactionSource = null,
            indication = null,
            enabled = enabled,
            onLongClick = onLongClick,
            onClick = onClick,
        )
    } else {
        Modifier.focusable(enabled)
    }
    Box(
        modifier = modifier
            .animateDpadFocus(glow = glow, shape = shape, focusedScale = focusedScale)
            .then(interaction),
        contentAlignment = contentAlignment,
        content = content,
    )
}
