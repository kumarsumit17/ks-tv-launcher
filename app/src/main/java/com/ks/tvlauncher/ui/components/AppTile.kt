package com.ks.tvlauncher.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
 * A 160x90 dp banner tile. [artwork] fills the tile (a banner, or an icon centred on the
 * glass); the tile itself never blurs, so it is safe in scrolling rows. The label below is
 * only visible while the tile is focused, and its space is always reserved so rows do not jump.
 */
@Composable
fun AppTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    glow: Color = Color.White,
    artwork: @Composable BoxScope.() -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    Column(modifier = modifier.width(KsDimens.TileWidth)) {
        DpadFocusable(
            modifier = Modifier
                .onFocusChanged { focused = it.hasFocus }
                .semantics { contentDescription = label },
            onClick = onClick,
            onLongClick = onLongClick,
            shape = KsShapes.Tile,
            glow = glow,
        ) {
            Box(
                modifier = Modifier
                    .size(KsDimens.TileWidth, KsDimens.TileHeight)
                    .glass(shape = KsShapes.Tile),
                contentAlignment = Alignment.Center,
                content = artwork,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = KsType.Caption,
            color = KsColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.alpha(if (focused) 1f else 0f),
        )
    }
}

/**
 * An unfilled slot in a tile row. It is a focus stop, so the row can be reached and
 * navigated before any app is pinned, but it has no action.
 */
@Composable
fun EmptyAppTile(contentDescription: String, modifier: Modifier = Modifier) {
    DpadFocusable(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        shape = KsShapes.Tile,
    ) {
        Box(
            Modifier
                .size(KsDimens.TileWidth, KsDimens.TileHeight)
                .glass(shape = KsShapes.Tile, fill = Color.White.copy(alpha = 0.05f)),
        )
    }
}
