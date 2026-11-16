package com.ks.tvlauncher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ks.tvlauncher.R
import com.ks.tvlauncher.ui.components.FrostSource
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsDimens
import com.ks.tvlauncher.ui.widgets.GreetingWidget
import kotlinx.coroutines.flow.Flow

/**
 * The Home shell: top bar, greeting widget, hero and the pinned row.
 * Stateless apart from scroll position and focus requesters.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    homeKeyEvents: Flow<Unit>,
    onSwitchProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowState = rememberLazyListState()
    val firstTileFocus = remember { FocusRequester() }

    // Initial focus goes to the first tile, once it has been laid out.
    LaunchedEffect(Unit) {
        withFrameNanos { }
        firstTileFocus.requestFocusSafely()
    }
    // Home key while already on Home: back to the start of the row.
    LaunchedEffect(homeKeyEvents) {
        homeKeyEvents.collect {
            rowState.scrollToItem(0)
            withFrameNanos { }
            firstTileFocus.requestFocusSafely()
        }
    }

    FrostSource(modifier = modifier.fillMaxSize(), background = { HomeBackground() }) {
        Column(Modifier.fillMaxSize().padding(vertical = KsDimens.OverscanVertical)) {
            HomeTopBar(
                time = state.time,
                shortDate = state.shortDate,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal),
            )
            Spacer(Modifier.height(16.dp))
            GreetingWidget(
                greeting = state.greeting,
                name = state.name,
                longDate = state.longDate,
                tvName = state.tvName,
                androidVersion = state.androidVersion,
                nextName = state.nextName,
                onSwitchProfile = onSwitchProfile,
                modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal).width(500.dp),
            )
            Spacer(Modifier.weight(1f))
            HomeHero(
                title = stringResource(R.string.hero_empty_title),
                body = stringResource(R.string.hero_empty_body),
                modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal),
            )
            Spacer(Modifier.height(18.dp))
            PinnedRow(
                listState = rowState,
                firstTileFocus = firstTileFocus,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * The plain Home background: the vertical navy gradient, a soft accent glow, and the dark
 * left and bottom overlays that keep text readable. Drawn in a single draw pass with
 * gradients, so it costs no bitmaps and no blur.
 */
@Composable
private fun HomeBackground() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(KsColors.BackgroundTop, KsColors.BackgroundBottom)))
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(KsColors.Accent.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width * 0.82f, size.height * 0.22f),
                        radius = size.width * 0.45f,
                    ),
                )
                drawRect(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent),
                        endX = size.width * 0.6f,
                    ),
                )
                drawRect(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                        startY = size.height * 0.55f,
                    ),
                )
            },
    )
}

/**
 * `requestFocus()` throws if the requester is not attached to a laid-out node yet, for
 * example when the first tile has not been composed. A launcher must never crash over
 * that: the request is simply skipped and the D-pad can still move focus normally.
 */
private fun FocusRequester.requestFocusSafely() {
    try {
        requestFocus()
    } catch (e: IllegalStateException) {
        // Not attached yet; nothing to focus.
    }
}
