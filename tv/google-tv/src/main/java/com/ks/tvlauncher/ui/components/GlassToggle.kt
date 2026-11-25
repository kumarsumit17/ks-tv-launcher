package com.ks.tvlauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType
import com.ks.tvlauncher.ui.theme.LocalMotion

/** A labelled on/off row. OK flips it. The thumb animation only redraws the small canvas. */
@Composable
fun GlassToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val position = animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = LocalMotion.current.focus(),
        label = "toggle",
    )
    val stateText = stringResource(if (checked) R.string.toggle_on else R.string.toggle_off)

    Row(
        modifier = modifier
            .animateDpadFocus(shape = KsShapes.Card, focusedScale = 1.02f)
            .toggleable(
                value = checked,
                interactionSource = null,
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .semantics { stateDescription = stateText }
            .glass(shape = KsShapes.Card)
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = KsType.Label, color = KsColors.TextPrimary, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Canvas(Modifier.size(width = 34.dp, height = 20.dp)) {
            val p = position.value
            val radius = size.height / 2f
            drawRoundRect(
                color = lerp(Color.White.copy(alpha = 0.22f), KsColors.Accent, p),
                cornerRadius = CornerRadius(radius, radius),
            )
            val inset = 3.dp.toPx()
            val thumbRadius = radius - inset
            val startX = radius
            val endX = size.width - radius
            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = Offset(startX + (endX - startX) * p, radius),
            )
        }
    }
}
