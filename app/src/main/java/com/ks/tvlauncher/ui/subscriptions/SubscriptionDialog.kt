package com.ks.tvlauncher.ui.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.domain.BillingCycle
import com.ks.tvlauncher.domain.Money
import com.ks.tvlauncher.domain.Subscription
import com.ks.tvlauncher.domain.SubscriptionCalculator
import com.ks.tvlauncher.ui.components.GlassButton
import com.ks.tvlauncher.ui.components.GlassChip
import com.ks.tvlauncher.ui.components.GlassDialog
import com.ks.tvlauncher.ui.components.GlassSearchField
import com.ks.tvlauncher.ui.components.GlassStepper
import com.ks.tvlauncher.ui.components.GlassToggle
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsType
import com.ks.tvlauncher.util.DateTimeFormats
import java.time.LocalDate
import java.util.UUID

private const val NAME_MAX = 40
private const val TEXT_MAX = 60
private const val STEP_MAJOR = 10L
private const val HOLD_STEP_MAJOR = 100L

/**
 * Add or edit one subscription. [existing] null means a new one. Everything is reachable with
 * the D-pad; Back cancels. Focus return is handled by the screen once the dialog has gone.
 */
@Composable
fun SubscriptionDialog(
    existing: Subscription?,
    currency: String,
    today: LocalDate,
    onSave: (Subscription) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    var provider by remember { mutableStateOf(existing?.provider.orEmpty()) }
    var costMinor by remember { mutableStateOf(existing?.costMinor ?: 0L) }
    var cycle by remember { mutableStateOf(existing?.cycle ?: BillingCycle.MONTHLY) }
    var renewsOn by remember {
        mutableStateOf(existing?.let { SubscriptionCalculator.nextRenewal(it, today) } ?: today)
    }
    var active by remember { mutableStateOf(existing?.active ?: true) }
    var note by remember { mutableStateOf(existing?.note.orEmpty()) }
    val perMajor = Money.minorPerMajor(currency)

    GlassDialog(
        onDismiss = onDismiss,
        title = stringResource(if (existing == null) R.string.subs_dialog_add else R.string.subs_dialog_edit),
        maxWidth = 560.dp,
    ) {
        GlassSearchField(
            query = name,
            onQueryChange = { name = it },
            placeholder = stringResource(R.string.subs_field_name),
            icon = null,
            imeAction = ImeAction.Done,
            maxLength = NAME_MAX,
            modifier = Modifier.fillMaxWidth(),
        )
        GlassSearchField(
            query = provider,
            onQueryChange = { provider = it },
            placeholder = stringResource(R.string.subs_field_provider),
            icon = null,
            imeAction = ImeAction.Done,
            maxLength = TEXT_MAX,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.subs_cost_label), style = KsType.Caption, color = KsColors.TextSecondary, modifier = Modifier.weight(1f))
            GlassStepper(
                value = Money.format(costMinor, currency),
                onStep = { direction, large ->
                    val step = (if (large) HOLD_STEP_MAJOR else STEP_MAJOR) * perMajor
                    costMinor = (costMinor + direction * step).coerceAtLeast(0L)
                },
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassChip(
                text = stringResource(R.string.subs_monthly),
                selected = cycle == BillingCycle.MONTHLY,
                onClick = { cycle = BillingCycle.MONTHLY },
            )
            GlassChip(
                text = stringResource(R.string.subs_yearly),
                selected = cycle == BillingCycle.YEARLY,
                onClick = { cycle = BillingCycle.YEARLY },
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.subs_renewal_label), style = KsType.Caption, color = KsColors.TextSecondary, modifier = Modifier.weight(1f))
            DateStep("«", stringResource(R.string.subs_prev_month)) { renewsOn = renewsOn.minusMonths(1) }
            DateStep("‹", stringResource(R.string.subs_prev_day)) { renewsOn = renewsOn.minusDays(1) }
            Text(
                text = DateTimeFormats.mediumDate(renewsOn),
                style = KsType.Label,
                color = KsColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.defaultMinSize(minWidth = 110.dp),
            )
            DateStep("›", stringResource(R.string.subs_next_day)) { renewsOn = renewsOn.plusDays(1) }
            DateStep("»", stringResource(R.string.subs_next_month)) { renewsOn = renewsOn.plusMonths(1) }
        }

        GlassToggle(label = stringResource(R.string.subs_active), checked = active, onCheckedChange = { active = it })

        GlassSearchField(
            query = note,
            onQueryChange = { note = it },
            placeholder = stringResource(R.string.subs_field_note),
            icon = null,
            imeAction = ImeAction.Done,
            maxLength = TEXT_MAX,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (existing != null) {
                GlassButton(text = stringResource(R.string.subs_delete), onClick = { onDelete(existing.id) })
            }
            Spacer(Modifier.weight(1f))
            GlassButton(text = stringResource(R.string.subs_cancel), onClick = onDismiss)
            GlassButton(
                text = stringResource(R.string.subs_save),
                icon = KsIcons.Check,
                primary = true,
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        Subscription(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            name = name,
                            provider = provider,
                            costMinor = costMinor,
                            cycle = cycle,
                            renewsOn = renewsOn.toString(),
                            active = active,
                            note = note,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun DateStep(symbol: String, description: String, onClick: () -> Unit) {
    GlassButton(text = symbol, onClick = onClick, modifier = Modifier.semantics { contentDescription = description })
}
