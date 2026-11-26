package com.ks.tvlauncher.ui.subscriptions

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.domain.BillingCycle
import com.ks.tvlauncher.domain.Money
import com.ks.tvlauncher.domain.NextRenewal
import com.ks.tvlauncher.domain.Subscription
import com.ks.tvlauncher.domain.SubscriptionCalculator
import com.ks.tvlauncher.domain.SubscriptionCsv
import com.ks.tvlauncher.ui.apps.AppIntents
import com.ks.tvlauncher.ui.components.FocusMemory
import com.ks.tvlauncher.ui.components.GlassButton
import com.ks.tvlauncher.ui.components.GlassCard
import com.ks.tvlauncher.ui.components.GlassPill
import com.ks.tvlauncher.ui.components.PillTone
import com.ks.tvlauncher.ui.components.modalFocus
import com.ks.tvlauncher.ui.components.requestFocusSafely
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsDimens
import com.ks.tvlauncher.ui.theme.KsType
import com.ks.tvlauncher.ui.theme.forText
import com.ks.tvlauncher.util.DateTimeFormats
import kotlinx.coroutines.delay

private const val DIALOG_SETTLE_MS = 120L

/** The dialog's target: a new subscription, or the one with this id. */
private sealed interface Editing {
    data object New : Editing
    data class Existing(val subscription: Subscription) : Editing
}

/**
 * Full-screen subscriptions list, drawn over Home. Back (or the Home button) calls [onClose].
 * Rows: OK opens the editor; Right moves to the Active/Paused pill, whose OK toggles it.
 */
@Composable
fun SubscriptionsScreen(
    state: SubscriptionsUiState,
    onClose: () -> Unit,
    onSave: (Subscription) -> Unit,
    onDelete: (String) -> Unit,
    onToggleActive: (String) -> Unit,
    exportCsv: () -> String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val addFocus = remember { FocusRequester() }
    val rowMemory = remember { FocusMemory() }
    var editing by remember { mutableStateOf<Editing?>(null) }
    var exportFailed by remember { mutableStateOf(false) }

    // Read through updated state so the focus effect sees rows saved or deleted meanwhile.
    val rowKeys by rememberUpdatedState(state.rows.map { it.subscription.id })

    BackHandler(enabled = editing == null, onBack = onClose)

    LaunchedEffect(Unit) {
        withFrameNanos { }
        addFocus.requestFocusSafely()
    }
    // After the dialog has gone, return focus to the row it came from (if it still exists),
    // else to Add subscription.
    var dialogWasOpen by remember { mutableStateOf(false) }
    LaunchedEffect(editing) {
        if (editing != null) {
            dialogWasOpen = true
        } else if (dialogWasOpen) {
            dialogWasOpen = false
            delay(DIALOG_SETTLE_MS)
            val keys = rowKeys
            val target = if (rowMemory.lastKey in keys) rowMemory.target(keys, addFocus) else addFocus
            target.requestFocusSafely()
        }
    }
    LaunchedEffect(exportFailed) {
        if (exportFailed) {
            delay(3_000)
            exportFailed = false
        }
    }

    Box(
        modifier
            .fillMaxSize()
            // Opaque: Home stays composed underneath and must not show through.
            .background(Brush.verticalGradient(listOf(KsColors.BackgroundTop, KsColors.BackgroundBottom)))
            .modalFocus(),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = KsDimens.OverscanHorizontal, vertical = KsDimens.OverscanVertical),
        ) {
            Header(
                addFocus = addFocus,
                canExport = state.rows.isNotEmpty(),
                onClose = onClose,
                onAdd = { editing = Editing.New },
                onExport = { if (!share(context, exportCsv())) exportFailed = true },
            )
            Spacer(Modifier.height(16.dp))
            StatCards(state)
            Spacer(Modifier.height(16.dp))
            GlassCard(modifier = Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(14.dp)) {
                ColumnHeaders()
                Spacer(Modifier.height(6.dp))
                if (state.loaded && state.rows.isEmpty()) {
                    Text(
                        stringResource(R.string.subs_empty),
                        style = KsType.Body,
                        color = KsColors.TextSecondary,
                        modifier = Modifier.padding(12.dp),
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 6.dp),
                    ) {
                        items(state.rows, key = { it.subscription.id }) { row ->
                            SubscriptionRowItem(
                                row = row,
                                currency = state.currency,
                                focusRequester = rowMemory.requester(row.subscription.id),
                                onFocused = { rowMemory.lastKey = row.subscription.id },
                                onOpen = { editing = Editing.Existing(row.subscription) },
                                onToggle = { onToggleActive(row.subscription.id) },
                            )
                        }
                    }
                }
            }
        }

        if (exportFailed) {
            Box(Modifier.fillMaxSize().padding(bottom = 36.dp), contentAlignment = Alignment.BottomCenter) {
                GlassPill(text = stringResource(R.string.subs_export_failed), tone = PillTone.Warning)
            }
        }

        editing?.let { target ->
            val existing = (target as? Editing.Existing)?.subscription
            SubscriptionDialog(
                existing = existing,
                currency = state.currency,
                today = state.today,
                onSave = {
                    onSave(it)
                    if (existing == null) rowMemory.lastKey = it.id
                    editing = null
                },
                onDelete = {
                    onDelete(it)
                    rowMemory.lastKey = null
                    editing = null
                },
                onDismiss = { editing = null },
            )
        }
    }
}

/** User-started share of the CSV (contract with ks-expense-manager). False if nothing can receive it. */
private fun share(context: Context, csv: String): Boolean {
    val send = Intent(Intent.ACTION_SEND)
        .setType(SubscriptionCsv.MIME_TYPE)
        .putExtra(Intent.EXTRA_TEXT, csv)
        .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.subs_export_subject))
    val chooser = Intent.createChooser(send, context.getString(R.string.subs_export_chooser))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return AppIntents.start(context, chooser)
}

@Composable
private fun Header(
    addFocus: FocusRequester,
    canExport: Boolean,
    onClose: () -> Unit,
    onAdd: () -> Unit,
    onExport: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        GlassButton(text = stringResource(R.string.subs_home), icon = KsIcons.ChevronLeft, onClick = onClose)
        Spacer(Modifier.width(16.dp))
        val title = stringResource(R.string.subs_title)
        Text(title, style = KsType.HeroTitle.forText(title), color = KsColors.TextPrimary, modifier = Modifier.weight(1f))
        GlassButton(text = stringResource(R.string.subs_export), onClick = onExport, enabled = canExport)
        Spacer(Modifier.width(12.dp))
        GlassButton(
            text = stringResource(R.string.subs_add),
            icon = KsIcons.Plus,
            primary = true,
            onClick = onAdd,
            modifier = Modifier.focusRequester(addFocus),
        )
    }
}

@Composable
private fun StatCards(state: SubscriptionsUiState) {
    val summary = state.summary
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        StatCard(stringResource(R.string.subs_stat_active), Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(summary.activeCount.toString(), style = KsType.HeroTitle, color = KsColors.TextPrimary)
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.subs_of_total, summary.totalCount),
                    style = KsType.Subtitle,
                    color = KsColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }
        StatCard(stringResource(R.string.subs_stat_month), Modifier.weight(1f)) {
            Text(Money.format(summary.monthlyMinor, state.currency), style = KsType.HeroTitle, color = KsColors.TextPrimary, maxLines = 1)
        }
        StatCard(stringResource(R.string.subs_stat_year), Modifier.weight(1f)) {
            Text(Money.format(summary.yearlyMinor, state.currency), style = KsType.HeroTitle, color = KsColors.TextPrimary, maxLines = 1)
        }
        StatCard(stringResource(R.string.subs_stat_next), Modifier.weight(1f), labelColor = KsColors.Warning) {
            val next = summary.next
            if (next == null) {
                Text(stringResource(R.string.subs_none_active), style = KsType.ScreenTitle, color = KsColors.TextSecondary)
            } else {
                Text(next.subscription.name, style = KsType.ScreenTitle, color = KsColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(nextRenewalLine(next), style = KsType.Caption, color = KsColors.Warning, maxLines = 1)
            }
        }
    }
}

/** "in 3 days · 19 Nov 2026" */
@Composable
fun nextRenewalLine(next: NextRenewal): String {
    val days = next.daysUntil.toInt()
    val relative = when (days) {
        0 -> stringResource(R.string.subs_today)
        1 -> stringResource(R.string.subs_tomorrow)
        else -> pluralStringResource(R.plurals.subs_in_days, days, days)
    }
    return stringResource(R.string.subs_next_line, relative, DateTimeFormats.mediumDate(next.date))
}

@Composable
private fun StatCard(
    label: String,
    modifier: Modifier,
    labelColor: androidx.compose.ui.graphics.Color = KsColors.TextSecondary,
    content: @Composable () -> Unit,
) {
    GlassCard(modifier = modifier.height(112.dp), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)) {
        Text(label, style = KsType.SectionLabel, color = labelColor)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

// Column weights shared by the header and the rows.
private const val W_NAME = 0.36f
private const val W_COST = 0.24f
private const val W_BILLING = 0.17f
private const val W_RENEWS = 0.23f

@Composable
private fun ColumnHeaders() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f)) {
            HeaderText(R.string.subs_col_name, W_NAME)
            HeaderText(R.string.subs_col_cost, W_COST)
            HeaderText(R.string.subs_col_billing, W_BILLING)
            HeaderText(R.string.subs_col_renews, W_RENEWS)
        }
        Box(Modifier.width(STATUS_WIDTH), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.subs_col_status), style = KsType.SectionLabel, color = KsColors.TextSecondary)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderText(res: Int, weight: Float) {
    Text(stringResource(res), style = KsType.SectionLabel, color = KsColors.TextSecondary, modifier = Modifier.weight(weight))
}

private val STATUS_WIDTH = 120.dp

@Composable
private fun SubscriptionRowItem(
    row: SubscriptionRow,
    currency: String,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
) {
    val sub = row.subscription
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassCard(
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) onFocused() },
            onClick = onOpen,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(W_NAME)) {
                    Text(sub.name, style = KsType.Label, color = KsColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (sub.provider.isNotEmpty()) {
                        Text(sub.provider, style = KsType.Caption, color = KsColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Column(Modifier.weight(W_COST)) {
                    Text(Money.format(sub.costMinor, currency), style = KsType.Label, color = KsColors.TextPrimary, maxLines = 1)
                    Text(equivalentText(sub, currency), style = KsType.Caption, color = KsColors.TextSecondary, maxLines = 1)
                }
                Text(
                    stringResource(if (sub.cycle == BillingCycle.MONTHLY) R.string.subs_monthly else R.string.subs_yearly),
                    style = KsType.Body,
                    color = KsColors.TextPrimary,
                    modifier = Modifier.weight(W_BILLING),
                )
                Text(
                    row.nextRenewal?.let { DateTimeFormats.mediumDate(it) } ?: sub.renewsOn,
                    style = KsType.Body,
                    color = KsColors.TextPrimary,
                    modifier = Modifier.weight(W_RENEWS),
                )
            }
        }
        Box(Modifier.width(STATUS_WIDTH), contentAlignment = Alignment.Center) {
            GlassPill(
                text = stringResource(if (sub.active) R.string.subs_active else R.string.subs_paused),
                tone = if (sub.active) PillTone.Success else PillTone.Neutral,
                onClick = onToggle,
            )
        }
    }
}

@Composable
private fun equivalentText(sub: Subscription, currency: String): String = when (sub.cycle) {
    BillingCycle.MONTHLY -> stringResource(R.string.subs_per_year, Money.format(SubscriptionCalculator.yearlyEquivalent(sub), currency))
    BillingCycle.YEARLY -> stringResource(R.string.subs_per_month_approx, Money.format(SubscriptionCalculator.monthlyEquivalent(sub), currency))
}
