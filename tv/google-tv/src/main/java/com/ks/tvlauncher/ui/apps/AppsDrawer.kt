package com.ks.tvlauncher.ui.apps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.domain.AppCategory
import com.ks.tvlauncher.domain.AppInfo
import com.ks.tvlauncher.ui.components.AppTile
import com.ks.tvlauncher.ui.components.FocusMemory
import com.ks.tvlauncher.ui.components.GlassButton
import com.ks.tvlauncher.ui.components.GlassChip
import com.ks.tvlauncher.ui.components.GlassSearchField
import com.ks.tvlauncher.ui.components.modalFocus
import com.ks.tvlauncher.ui.components.requestFocusSafely
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsDimens
import com.ks.tvlauncher.ui.theme.KsType

private const val COLUMNS = 6

/**
 * The All apps drawer: a full-screen overlay with search, category filter, sort toggle and a
 * 6-column lazy grid. Stateless; state comes from [AppsDrawerViewModel].
 *
 * It is modal (the D-pad cannot reach Home behind it), Back closes it, and on close focus
 * returns to [returnFocusTo], the control that opened it. Its background is an opaque
 * gradient, so nothing is blurred behind the scrolling grid. [overlay] is for the app menu.
 */
@Composable
fun AppsDrawer(
    state: AppsDrawerUiState,
    focusMemory: FocusMemory,
    fallbackFocus: FocusRequester,
    returnFocusTo: FocusRequester,
    onClose: () -> Unit,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (AppCategory?) -> Unit,
    onToggleSort: () -> Unit,
    onToggleEdit: () -> Unit,
    onOpenApp: (AppInfo) -> Unit,
    onOpenMenu: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    overlay: @Composable () -> Unit = {},
) {
    val keys = remember(state.apps) { state.apps.map { it.app.packageName } }

    BackHandler(onBack = onClose)
    // Focus moves into the drawer: the tile focused last (else the first), or the Home
    // button ([fallbackFocus]) while there are no tiles.
    LaunchedEffect(state.loaded) {
        withFrameNanos { }
        focusMemory.target(keys, fallbackFocus).requestFocusSafely()
    }
    DisposableEffect(returnFocusTo) {
        onDispose { returnFocusTo.requestFocusSafely() }
    }

    // [overlay] (the app menu) is drawn inside the drawer's modal focus group, so focus can
    // move from the grid into the menu and back without ever leaving the drawer.
    Box(modifier = modifier.fillMaxSize().modalFocus()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(KsColors.BackgroundTop, KsColors.BackgroundBottom)))
            .padding(vertical = KsDimens.OverscanVertical),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal).focusGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassButton(
                    text = stringResource(R.string.drawer_back),
                    onClick = onClose,
                    icon = KsIcons.ChevronLeft,
                    modifier = Modifier.focusRequester(fallbackFocus),
                )
                Spacer(Modifier.width(14.dp))
                Text(stringResource(R.string.all_apps), style = KsType.ScreenTitle, color = KsColors.TextPrimary)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.drawer_counts, state.installedCount, state.pinnedCount),
                    style = KsType.Body,
                    color = KsColors.TextSecondary,
                )
                Spacer(Modifier.weight(1f))
                GlassChip(
                    text = stringResource(if (state.editMode) R.string.drawer_edit_done else R.string.drawer_edit),
                    selected = state.editMode,
                    onClick = onToggleEdit,
                    icon = if (state.editMode) KsIcons.Check else KsIcons.Sliders,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassSearchField(
                    query = state.query,
                    onQueryChange = onQueryChange,
                    placeholder = stringResource(R.string.drawer_search_hint),
                    modifier = Modifier.width(300.dp),
                )
                if (state.categories.isNotEmpty()) {
                    GlassChip(
                        text = stringResource(R.string.drawer_filter_all),
                        selected = state.category == null,
                        onClick = { onCategoryChange(null) },
                    )
                    state.categories.forEach { category ->
                        GlassChip(
                            text = stringResource(category.labelRes()),
                            selected = state.category == category,
                            onClick = { onCategoryChange(category) },
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                GlassChip(
                    text = stringResource(if (state.ascending) R.string.drawer_sort_az else R.string.drawer_sort_za),
                    selected = false,
                    onClick = onToggleSort,
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        if (state.loaded && state.apps.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(
                        if (state.installedCount == 0) R.string.drawer_no_apps else R.string.drawer_no_match,
                    ),
                    style = KsType.Subtitle,
                    color = KsColors.TextSecondary,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(COLUMNS),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .focusGroup(),
                // Room for the focused tile's scale and glow at every edge of the grid.
                contentPadding = PaddingValues(horizontal = KsDimens.OverscanHorizontal, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(KsDimens.TileGap),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.apps, key = { it.app.packageName }, contentType = { "app" }) { entry ->
                    val app = entry.app
                    AppTile(
                        label = app.label,
                        onClick = { if (state.editMode) onOpenMenu(app) else onOpenApp(app) },
                        onLongClick = { onOpenMenu(app) },
                        onFocused = { focusMemory.lastKey = app.packageName },
                        modifier = Modifier.focusRequester(focusMemory.requester(app.packageName)),
                        fillWidth = true,
                        labelAlwaysVisible = true,
                        badge = if (entry.pinned) stringResource(R.string.drawer_pinned_badge) else null,
                    ) {
                        AppArtworkImage(app)
                    }
                }
            }
        }

        Text(
            text = stringResource(if (state.editMode) R.string.drawer_hint_edit else R.string.drawer_hint),
            style = KsType.Caption,
            color = KsColors.TextTertiary,
            modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal),
        )
    }
    overlay()
    }
}

private fun AppCategory.labelRes(): Int = when (this) {
    AppCategory.VIDEO -> R.string.category_video
    AppCategory.MUSIC -> R.string.category_music
    AppCategory.GAMES -> R.string.category_games
    AppCategory.TOOLS -> R.string.category_tools
}
