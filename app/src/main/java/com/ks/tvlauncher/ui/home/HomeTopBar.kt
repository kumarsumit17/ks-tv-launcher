package com.ks.tvlauncher.ui.home

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.ui.components.GlassIconButton
import com.ks.tvlauncher.ui.components.GlassPill
import com.ks.tvlauncher.ui.components.glass
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType

/** Brand, live clock, short date, weather pill and the settings button. */
@Composable
fun HomeTopBar(
    time: String,
    shortDate: String?,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().focusGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.glass(shape = KsShapes.Badge).size(26.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.brand_badge), style = KsType.CaptionSmall, color = KsColors.TextPrimary)
        }
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.brand_title), style = KsType.Label, color = KsColors.TextPrimary)

        Spacer(Modifier.weight(1f))

        Text(time, style = KsType.Clock, color = KsColors.TextPrimary)
        if (shortDate != null) {
            Spacer(Modifier.width(8.dp))
            Text(shortDate, style = KsType.Body, color = KsColors.TextSecondary)
        }
        Spacer(Modifier.width(14.dp))
        // Display-only until weather is set up; it is not a focus stop.
        GlassPill(
            text = stringResource(R.string.weather_not_set),
            icon = KsIcons.Cloud,
            frosted = true,
            modifier = Modifier.alpha(0.7f),
        )
        Spacer(Modifier.width(10.dp))
        GlassIconButton(
            icon = KsIcons.Gear,
            contentDescription = stringResource(R.string.open_android_settings),
            onClick = onOpenSettings,
        )
    }
}
