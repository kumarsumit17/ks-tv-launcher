package com.ks.tvlauncher.ui.subscriptions

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ks.tvlauncher.KsApplication
import com.ks.tvlauncher.data.settings.SettingsRepository
import com.ks.tvlauncher.data.subscriptions.SubscriptionRepository
import com.ks.tvlauncher.domain.Money
import com.ks.tvlauncher.domain.Subscription
import com.ks.tvlauncher.domain.SubscriptionCalculator
import com.ks.tvlauncher.domain.SubscriptionCsv
import com.ks.tvlauncher.domain.SubscriptionSummary
import com.ks.tvlauncher.util.minuteTicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** One row of the list, with the renewal already rolled forward. */
@Immutable
data class SubscriptionRow(
    val subscription: Subscription,
    val nextRenewal: LocalDate?,
)

@Immutable
data class SubscriptionsUiState(
    val loaded: Boolean = false,
    val currency: String = Money.DEFAULT_CURRENCY,
    val today: LocalDate = LocalDate.now(),
    val rows: List<SubscriptionRow> = emptyList(),
    val summary: SubscriptionSummary = SubscriptionCalculator.summary(emptyList(), LocalDate.now()),
)

/**
 * Subscriptions joined with the currency and today's date. "Today" follows the minute-tick
 * broadcast, so renewals roll forward at midnight without polling. Shared by this screen and
 * the Home card so both show the same numbers.
 */
fun subscriptionsState(
    context: Context,
    repository: SubscriptionRepository,
    settingsRepository: SettingsRepository,
): Flow<SubscriptionsUiState> = combine(
    repository.subscriptions,
    settingsRepository.settings.map { it.currency }.distinctUntilChanged(),
    minuteTicker(context).map { it.toLocalDate() }.distinctUntilChanged(),
) { subs, currency, today ->
    SubscriptionsUiState(
        loaded = true,
        currency = currency,
        today = today,
        rows = subs.map { SubscriptionRow(it, SubscriptionCalculator.nextRenewal(it, today)) },
        summary = SubscriptionCalculator.summary(subs, today),
    )
}.flowOn(Dispatchers.Default)

class SubscriptionsViewModel(
    appContext: Context,
    private val repository: SubscriptionRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SubscriptionsUiState> = subscriptionsState(appContext, repository, settingsRepository)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubscriptionsUiState())

    fun save(subscription: Subscription) {
        viewModelScope.launch { repository.upsert(subscription.copy(name = subscription.name.trim(), provider = subscription.provider.trim(), note = subscription.note.trim())) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }

    fun toggleActive(id: String) {
        viewModelScope.launch { repository.toggleActive(id) }
    }

    /** The CSV for the share sheet, from what is on screen now. */
    fun exportCsv(): String {
        val state = uiState.value
        return SubscriptionCsv.export(state.rows.map { it.subscription }, state.currency, state.today)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as KsApplication
                SubscriptionsViewModel(app, app.container.subscriptionRepository, app.container.settingsRepository)
            }
        }
    }
}
