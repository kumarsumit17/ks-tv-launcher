package com.ks.tvlauncher.ui.widgets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.domain.GreetingPeriod
import com.ks.tvlauncher.ui.components.GlassCard
import com.ks.tvlauncher.ui.components.GlassPill
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType

private val NameBrush = Brush.horizontalGradient(listOf(KsColors.NameGradientStart, KsColors.NameGradientEnd))

/**
 * "Good evening, Sumit" · the long date · the TV's own name and Android version.
 * Stateless: every value comes from the Home state, which refreshes on the minute ticker,
 * so the greeting changes at 12:00 and 17:00 without a restart.
 */
@Composable
fun GreetingWidget(
    greeting: GreetingPeriod,
    name: String?,
    longDate: String,
    tvName: String,
    androidVersion: String,
    nextName: String?,
    onSwitchProfile: () -> Unit,
    modifier: Modifier = Modifier,
    /** Holding OK on the switch chip (Control Center). */
    onLongPress: (() -> Unit)? = null,
    chipModifier: Modifier = Modifier,
) {
    val greetingText = stringResource(
        when (greeting) {
            GreetingPeriod.MORNING -> R.string.greeting_morning
            GreetingPeriod.AFTERNOON -> R.string.greeting_afternoon
            GreetingPeriod.EVENING -> R.string.greeting_evening
        },
    )
    val headline: AnnotatedString = remember(greetingText, name) {
        buildAnnotatedString {
            append(greetingText)
            if (name != null) {
                append(", ")
                withStyle(SpanStyle(brush = NameBrush)) { append(name) }
            }
        }
    }

    GlassCard(
        modifier = modifier,
        shape = KsShapes.Panel,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = headline,
            style = KsType.Greeting,
            color = KsColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(longDate, style = KsType.Subtitle, color = KsColors.TextSecondary)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (tvName.isNotEmpty()) {
                Icon(KsIcons.Tv, contentDescription = null, modifier = Modifier.size(14.dp), tint = KsColors.TextSecondary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = deviceLine(tvName, androidVersion),
                    style = KsType.Body,
                    color = KsColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (nextName != null) {
                Spacer(Modifier.width(12.dp))
                GlassPill(
                    text = stringResource(R.string.switch_to, nextName),
                    icon = KsIcons.Person,
                    onClick = onSwitchProfile,
                    onLongClick = onLongPress,
                    modifier = chipModifier,
                )
            }
        }
    }
}

/** "‹TV name› · Android ‹version›" with the TV name emphasised. */
@Composable
private fun deviceLine(tvName: String, androidVersion: String): AnnotatedString {
    val full = stringResource(R.string.device_line, tvName, androidVersion)
    return remember(full, tvName) {
        val start = full.indexOf(tvName)
        buildAnnotatedString {
            append(full)
            if (start >= 0) {
                addStyle(
                    SpanStyle(color = KsColors.TextPrimary, fontWeight = FontWeight.SemiBold),
                    start,
                    start + tvName.length,
                )
            }
        }
    }
}
