package com.ks.tvlauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsDimens
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType

/**
 * A 16:9 app tile. [artwork] fills the tile (a banner, or an icon centred on the glass). The
 * tile itself never blurs, so it is safe in scrolling rows and grids.
 *
 * - Row use: fixed 160x90 dp, and the label shows only while the tile is focused. Its space is
 *   always reserved so the row does not jump.
 * - Grid use ([fillWidth]): takes the cell's width, with the label always visible
 *   ([labelAlwaysVisible]) and an optional [badge] after it.
 *
 * OK calls [onClick]; holding OK calls [onLongClick]. [onFocused] fires when the tile gains focus.
 */
@Composable
fun AppTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onFocused: (() -> Unit)? = null,
    glow: Color = Color.White,
    fillWidth: Boolean = false,
    labelAlwaysVisible: Boolean = false,
    badge: String? = null,
    artwork: @Composable BoxScope.() -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val widthModifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier.width(KsDimens.TileWidth)

    Column(modifier = modifier.then(widthModifier)) {
        DpadFocusable(
            modifier = Modifier
                .onFocusChanged {
                    val nowFocused = it.hasFocus
                    if (nowFocused && !focused) onFocused?.invoke()
                    focused = nowFocused
                }
                .semantics { contentDescription = label },
            onClick = onClick,
            onLongClick = onLongClick,
            shape = KsShapes.Tile,
            glow = glow,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .glass(shape = KsShapes.Tile),
                contentAlignment = Alignment.Center,
                content = artwork,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.alpha(if (labelAlwaysVisible || focused) 1f else 0f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = KsType.Caption,
                color = if (focused) KsColors.TextPrimary else KsColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (badge != null) {
                Text(
                    text = badge,
                    style = KsType.CaptionSmall,
                    color = KsColors.TextTertiary,
                    maxLines = 1,
                    modifier = Modifier
                        .glass(shape = KsShapes.Pill)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                )
            }
        }
    }
}
