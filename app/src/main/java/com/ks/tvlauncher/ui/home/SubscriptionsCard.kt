package com.ks.tvlauncher.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.domain.Money
import com.ks.tvlauncher.ui.components.GlassCard
import com.ks.tvlauncher.ui.components.GlassPill
import com.ks.tvlauncher.ui.components.PillTone
import com.ks.tvlauncher.ui.subscriptions.SubscriptionsUiState
import com.ks.tvlauncher.ui.subscriptions.nextRenewalLine
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsType

/**
 * Bottom-strip card: monthly and yearly totals, active count and the next renewal, from the
 * same state as the Subscriptions screen. OK opens that screen.
 */
@Composable
fun SubscriptionsCard(state: SubscriptionsUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val summary = state.summary
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.subs_card_title) + "  " + summary.activeCount,
                    style = KsType.Label,
                    color = KsColors.TextPrimary,
                    maxLines = 1,
                )
                Text(
                    text = if (summary.totalCount == 0) {
                        stringResource(R.string.subs_card_empty)
                    } else {
                        stringResource(
                            R.string.subs_card_totals,
                            Money.format(summary.monthlyMinor, state.currency),
                            Money.format(summary.yearlyMinor, state.currency),
                        )
                    },
                    style = KsType.Caption,
                    color = KsColors.TextSecondary,
                    maxLines = 1,
                )
            }
            summary.next?.let { next ->
                Spacer(Modifier.width(10.dp))
                GlassPill(text = next.subscription.name + " · " + nextRenewalLine(next), tone = PillTone.Warning)
            }
        }
    }
}
