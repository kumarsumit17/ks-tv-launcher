package com.ks.tvlauncher.ui.home

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.domain.AppInfo
import com.ks.tvlauncher.ui.apps.AppArtworkImage
import com.ks.tvlauncher.ui.components.AppTile
import com.ks.tvlauncher.ui.components.FocusMemory
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsDimens
import com.ks.tvlauncher.ui.theme.KsType
import com.ks.tvlauncher.ui.theme.LocalMotion

/**
 * The pinned apps: a lazy row of 160x90 dp banner tiles, so only visible tiles are composed.
 *
 * Focus: [focusMemory] holds a requester per tile and remembers the tile focused last, so
 * other controls can send focus back to exactly that tile. From every tile, Up goes to the hero's Open button and Down to the bottom
 * strip, so vertical moves land in the same place whichever tile they start from. The content padding
 * keeps the scaled, glowing focused tile inside the row's clip and the overscan-safe area,
 * and scrolling keeps the focused tile fully visible.
 */
@Composable
fun PinnedRow(
    apps: List<AppInfo>,
    listState: LazyListState,
    focusMemory: FocusMemory,
    upFocus: FocusRequester,
    downFocus: FocusRequester,
    onOpen: (AppInfo) -> Unit,
    onOpenMenu: (AppInfo) -> Unit,
    onTileFocused: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val animate = LocalMotion.current.enabled
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.pinned_apps).uppercase(),
            style = KsType.SectionLabel,
            color = KsColors.TextTertiary,
            modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal),
        )
        LazyRow(
            state = listState,
            modifier = Modifier.focusGroup(),
            contentPadding = PaddingValues(
                start = KsDimens.OverscanHorizontal,
                end = KsDimens.OverscanHorizontal,
                top = 10.dp,
                bottom = 4.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(KsDimens.TileGap),
        ) {
            items(apps, key = { it.packageName }, contentType = { "app" }) { app ->
                AppTile(
                    label = app.label,
                    onClick = { onOpen(app) },
                    onLongClick = { onOpenMenu(app) },
                    onFocused = {
                        focusMemory.lastKey = app.packageName
                        onTileFocused(app)
                    },
                    // Tiles slide when the order changes, unless motion is switched off.
                    modifier = (if (animate) Modifier.animateItem() else Modifier)
                        // Set on the tile itself: focus properties on the row container
                        // do not apply to moves that start from an item inside it.
                        .focusRequester(focusMemory.requester(app.packageName))
                        .focusProperties {
                            up = upFocus
                            down = downFocus
                        },
                ) {
                    AppArtworkImage(app)
                }
            }
        }
    }
}
