package com.ks.tvlauncher.ui.home

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.ui.components.EmptyAppTile
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsDimens
import com.ks.tvlauncher.ui.theme.KsType

/** Enough slots to fill the row and scroll one tile past the safe area. */
private const val EMPTY_SLOTS = 6

/**
 * The pinned-apps row in its empty state: focusable slots with no action.
 *
 * It is a lazy row, so only visible tiles are composed. The content padding keeps the
 * scaled, glowing focused tile inside the row's clip and inside the overscan-safe area.
 */
@Composable
fun PinnedRow(
    listState: LazyListState,
    firstTileFocus: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.pinned_apps),
            style = KsType.SectionLabel,
            color = KsColors.TextTertiary,
            modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal),
        )
        LazyRow(
            state = listState,
            modifier = Modifier.focusRestorer(firstTileFocus).focusGroup(),
            contentPadding = PaddingValues(horizontal = KsDimens.OverscanHorizontal, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(KsDimens.TileGap),
        ) {
            items(count = EMPTY_SLOTS, key = { it }, contentType = { "empty" }) { index ->
                EmptyAppTile(
                    contentDescription = stringResource(R.string.pinned_slot, index + 1),
                    modifier = if (index == 0) Modifier.focusRequester(firstTileFocus) else Modifier,
                )
            }
        }
    }
}
