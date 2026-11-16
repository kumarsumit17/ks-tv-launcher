package com.ks.tvlauncher.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.ks.tvlauncher.ui.theme.KsShapes

/**
 * A glass container. With [onClick] it becomes a D-pad focus stop with the standard focus
 * treatment; without it, it is a passive surface.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    shape: Shape = KsShapes.Card,
    frosted: Boolean = false,
    glow: Color = Color.White,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick == null) {
        Column(
            modifier = modifier.glass(shape = shape, frosted = frosted).padding(contentPadding),
            content = content,
        )
    } else {
        DpadFocusable(
            modifier = modifier,
            onClick = onClick,
            onLongClick = onLongClick,
            shape = shape,
            glow = glow,
            focusedScale = 1.03f,
        ) {
            Column(
                modifier = Modifier.glass(shape = shape, frosted = frosted).padding(contentPadding),
                content = content,
            )
        }
    }
}
