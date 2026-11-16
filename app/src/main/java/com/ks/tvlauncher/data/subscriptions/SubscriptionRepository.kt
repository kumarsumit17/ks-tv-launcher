package com.ks.tvlauncher.data.subscriptions

import com.ks.tvlauncher.domain.Subscription
import kotlinx.coroutines.flow.Flow

/** Subscriptions on this device. No accounts, no sync. */
class SubscriptionRepository(private val dataStore: SubscriptionDataStore) {

    val subscriptions: Flow<List<Subscription>> = dataStore.subscriptions

    /** Replaces the subscription with the same id, or adds it at the end. */
    suspend fun upsert(subscription: Subscription) = dataStore.update { list ->
        if (list.any { it.id == subscription.id }) {
            list.map { if (it.id == subscription.id) subscription else it }
        } else {
            list + subscription
        }
    }

    suspend fun delete(id: String) = dataStore.update { list -> list.filterNot { it.id == id } }

    suspend fun toggleActive(id: String) = dataStore.update { list ->
        list.map { if (it.id == id) it.copy(active = !it.active) else it }
    }
}
