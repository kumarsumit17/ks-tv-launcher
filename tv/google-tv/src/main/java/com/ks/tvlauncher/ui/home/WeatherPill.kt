package com.ks.tvlauncher.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.ks.tvlauncher.R
import com.ks.tvlauncher.data.weather.WeatherState
import com.ks.tvlauncher.domain.TemperatureUnit
import com.ks.tvlauncher.domain.WeatherCodes
import com.ks.tvlauncher.domain.WeatherGroup
import com.ks.tvlauncher.ui.components.GlassPill
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.LocalMotion

/**
 * The frosted weather pill in the top bar. A focus stop: OK opens Settings at Weather.
 * Loading pulses gently (still with motion off); a stale reading is dimmed.
 */
@Composable
fun WeatherPill(
    state: WeatherState,
    unit: TemperatureUnit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val text: String
    val icon: ImageVector
    var dim = 1f
    var pulsing = false
    when (state) {
        WeatherState.NoCity -> {
            text = stringResource(R.string.weather_set_city)
            icon = KsIcons.Cloud
        }
        WeatherState.Loading -> {
            text = stringResource(R.string.weather_loading)
            icon = KsIcons.Cloud
            pulsing = true
        }
        WeatherState.Unavailable -> {
            text = stringResource(R.string.weather_unavailable)
            icon = KsIcons.Cloud
            dim = 0.6f
        }
        is WeatherState.Ready -> {
            val group = WeatherCodes.group(state.weather.weatherCode)
            text = stringResource(
                R.string.weather_pill,
                WeatherCodes.temperature(state.weather.temperatureC, unit),
                stringResource(groupLabel(group)),
                state.city.name,
            )
            icon = groupIcon(group)
            if (state.stale) dim = 0.55f
        }
    }

    val pulseModifier = if (pulsing && LocalMotion.current.enabled) {
        val transition = rememberInfiniteTransition(label = "weatherShimmer")
        val alpha = transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "weatherShimmerAlpha",
        )
        // Read only in the layer phase: the pulse redraws without recomposing.
        Modifier.graphicsLayer { this.alpha = alpha.value }
    } else {
        Modifier.graphicsLayer { alpha = if (pulsing) 0.6f else dim }
    }

    GlassPill(
        text = text,
        icon = icon,
        frosted = true,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.then(pulseModifier),
    )
}

fun groupIcon(group: WeatherGroup): ImageVector = when (group) {
    WeatherGroup.CLEAR -> KsIcons.Sun
    WeatherGroup.CLOUDY -> KsIcons.Cloud
    WeatherGroup.FOG -> KsIcons.Fog
    WeatherGroup.RAIN -> KsIcons.Rain
    WeatherGroup.SNOW -> KsIcons.Snow
    WeatherGroup.STORM -> KsIcons.Storm
}

fun groupLabel(group: WeatherGroup): Int = when (group) {
    WeatherGroup.CLEAR -> R.string.weather_clear
    WeatherGroup.CLOUDY -> R.string.weather_cloudy
    WeatherGroup.FOG -> R.string.weather_fog
    WeatherGroup.RAIN -> R.string.weather_rain
    WeatherGroup.SNOW -> R.string.weather_snow
    WeatherGroup.STORM -> R.string.weather_storm
}
