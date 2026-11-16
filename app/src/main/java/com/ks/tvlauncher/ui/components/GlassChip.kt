package com.ks.tvlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType

/** A pill that is either selected (solid light fill) or not (glass). Used for filters and toggles. */
@Composable
fun GlassChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val contentColor = if (selected) KsColors.OnPrimaryButton else KsColors.TextPrimary
    DpadFocusable(
        modifier = modifier.semantics { this.selected = selected },
        onClick = onClick,
        shape = KsShapes.Pill,
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (selected) {
                        Modifier.clip(KsShapes.Pill).background(KsColors.PrimaryButton)
                    } else {
                        Modifier.glass(shape = KsShapes.Pill)
                    },
                )
                .defaultMinSize(minHeight = 30.dp)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = contentColor)
            }
            Text(text, style = KsType.Label, color = contentColor, maxLines = 1)
        }
    }
}
