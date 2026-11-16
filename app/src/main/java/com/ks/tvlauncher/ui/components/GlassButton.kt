package com.ks.tvlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType

/** Pill-shaped button. [primary] is the solid light button; otherwise it is glass. */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    primary: Boolean = false,
    enabled: Boolean = true,
) {
    val contentColor = if (primary) KsColors.OnPrimaryButton else KsColors.TextPrimary
    DpadFocusable(
        modifier = modifier.semantics { role = Role.Button },
        onClick = onClick,
        enabled = enabled,
        shape = KsShapes.Pill,
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (primary) {
                        Modifier.clip(KsShapes.Pill).background(KsColors.PrimaryButton)
                    } else {
                        Modifier.glass(shape = KsShapes.Pill)
                    },
                )
                .defaultMinSize(minHeight = 34.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = contentColor)
            }
            Text(text, style = KsType.Label, color = contentColor, maxLines = 1)
        }
    }
}

/** Round icon-only button, e.g. the gear in the top bar. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DpadFocusable(
        modifier = modifier.semantics { role = Role.Button },
        onClick = onClick,
        shape = KsShapes.Pill,
        focusedScale = 1.1f,
    ) {
        Box(
            modifier = Modifier.glass(shape = KsShapes.Pill).size(30.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(16.dp), tint = KsColors.TextPrimary)
        }
    }
}
