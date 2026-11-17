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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.ui.components.GlassIconButton
import com.ks.tvlauncher.ui.components.GlassPill
import com.ks.tvlauncher.ui.components.PillTone
import com.ks.tvlauncher.data.weather.WeatherState
import com.ks.tvlauncher.domain.TemperatureUnit
import com.ks.tvlauncher.ui.components.glass
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType

/**
 * Brand, live clock, short date, weather pill and the settings button. Holding OK on the pill
 * or the gear opens Control Center ([onLongPress]).
 */
@Composable
fun HomeTopBar(
    time: String,
    shortDate: String?,
    weather: WeatherState,
    temperatureUnit: TemperatureUnit,
    onOpenWeather: () -> Unit,
    onOpenSettings: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    weatherModifier: Modifier = Modifier,
    gearModifier: Modifier = Modifier,
    /** Pending update's version; null hides the pill. */
    updateVersion: String? = null,
    onOpenUpdate: () -> Unit = {},
    updateModifier: Modifier = Modifier,
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
        if (updateVersion != null) {
            // TV notifications are easy to miss: OK opens Settings › About.
            GlassPill(
                text = stringResource(R.string.update_pill),
                icon = KsIcons.ArrowUpRight,
                // Not frosted: blur stays limited to the weather pill and dialogs.
                tone = PillTone.Success,
                onClick = onOpenUpdate,
                onLongClick = onLongPress,
                modifier = updateModifier,
            )
            Spacer(Modifier.width(10.dp))
        }
        WeatherPill(
            state = weather,
            unit = temperatureUnit,
            onClick = onOpenWeather,
            onLongClick = onLongPress,
            modifier = weatherModifier,
        )
        Spacer(Modifier.width(10.dp))
        GlassIconButton(
            icon = KsIcons.Gear,
            contentDescription = stringResource(R.string.open_settings),
            onClick = onOpenSettings,
            onLongClick = onLongPress,
            modifier = gearModifier,
        )
    }
}
