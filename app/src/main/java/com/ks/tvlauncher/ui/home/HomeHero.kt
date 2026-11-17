package com.ks.tvlauncher.ui.home

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.domain.AppInfo
import com.ks.tvlauncher.ui.components.GlassButton
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsType

/**
 * The large block above the pinned row. With a pinned app it shows that app's name and the
 * "Open ‹app›" and "Unpin" buttons; with none it explains how to pin one.
 *
 * Down from the buttons goes to [downFocus], the pinned tile that was focused last.
 */
@Composable
fun HomeHero(
    app: AppInfo?,
    openFocus: FocusRequester,
    downFocus: () -> FocusRequester,
    onOpen: (AppInfo) -> Unit,
    onUnpin: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    /** Holding OK on either button (Control Center). */
    onLongPress: (() -> Unit)? = null,
    unpinFocus: FocusRequester = remember { FocusRequester() },
    /** Called with true for Open, false for Unpin, when a button gains focus. */
    onButtonFocused: (open: Boolean) -> Unit = {},
) {
    Column(modifier = modifier) {
        Text(
            text = app?.label ?: stringResource(R.string.hero_empty_title),
            style = KsType.HeroTitle,
            color = KsColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(if (app != null) R.string.hero_pinned_body else R.string.hero_empty_body),
            style = KsType.Body,
            color = KsColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 520.dp),
        )
        if (app != null) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.focusGroup(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GlassButton(
                    text = stringResource(R.string.hero_open, app.label),
                    onClick = { onOpen(app) },
                    icon = KsIcons.Play,
                    primary = true,
                    onLongClick = onLongPress,
                    modifier = Modifier
                        .focusRequester(openFocus)
                        .onFocusChanged { if (it.isFocused) onButtonFocused(true) }
                        .focusProperties { down = downFocus() },
                )
                GlassButton(
                    text = stringResource(R.string.hero_unpin),
                    onClick = { onUnpin(app) },
                    onLongClick = onLongPress,
                    modifier = Modifier
                        .focusRequester(unpinFocus)
                        .onFocusChanged { if (it.isFocused) onButtonFocused(false) }
                        .focusProperties { down = downFocus() },
                )
            }
        }
    }
}
