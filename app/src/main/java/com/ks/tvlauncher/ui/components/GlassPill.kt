package com.ks.tvlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType

enum class PillTone { Neutral, Warning, Success, Danger }

/**
 * Fully rounded chip for status and short actions. With [onClick] it is a focus stop;
 * without it, it is display-only and skipped by the D-pad. [frosted] blurs what is behind
 * it and is meant for still pills such as the weather pill in the top bar.
 */
@Composable
fun GlassPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tone: PillTone = PillTone.Neutral,
    frosted: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val contentColor: Color
    val container: Color?
    when (tone) {
        PillTone.Neutral -> { contentColor = KsColors.TextPrimary; container = null }
        PillTone.Warning -> { contentColor = KsColors.Warning; container = KsColors.WarningContainer }
        PillTone.Success -> { contentColor = KsColors.Success; container = KsColors.SuccessContainer }
        PillTone.Danger -> { contentColor = KsColors.Danger; container = KsColors.DangerContainer }
    }

    val body: @Composable (Modifier) -> Unit = { outer ->
        Row(
            modifier = outer
                .then(
                    if (container == null) {
                        Modifier.glass(shape = KsShapes.Pill, frosted = frosted)
                    } else {
                        Modifier.clip(KsShapes.Pill).background(container)
                    },
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = contentColor)
            }
            Text(text, style = KsType.Caption, color = contentColor, maxLines = 1)
        }
    }

    if (onClick == null) {
        body(modifier)
    } else {
        DpadFocusable(modifier = modifier, onClick = onClick, onLongClick = onLongClick, shape = KsShapes.Pill) { body(Modifier) }
    }
}
