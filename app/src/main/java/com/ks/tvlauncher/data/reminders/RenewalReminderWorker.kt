package com.ks.tvlauncher.data.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ks.tvlauncher.KsApplication
import com.ks.tvlauncher.domain.RenewalReminders
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * The daily check (also run once after a subscription or reminder setting changes). Reads the
 * list, notifies renewals inside the window once, and withdraws reminders that no longer
 * apply. WorkManager decides when it runs, so timing is approximate.
 */
class RenewalReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as KsApplication).container
        val notifier = ReminderNotifier(applicationContext)
        val store = container.reminderStore
        val settings = container.settingsRepository.settings.first()
        if (!settings.remindersEnabled) {
            notifier.cancelAll()
            store.setNotified(emptySet())
            return Result.success()
        }
        val subs = container.subscriptionRepository.subscriptions.first()
        val plan = RenewalReminders.plan(subs, LocalDate.now(), settings.reminderLeadDays, store.notified())
        // Withdraw first, so a fresh reminder for the same subscription is not removed.
        plan.toCancel.forEach(notifier::cancel)
        if (notifier.canNotify()) {
            plan.toNotify.forEach { notifier.post(it, settings.currency) }
            store.setNotified(plan.notified)
        } else {
            // Blocked: remember nothing new, so the reminder comes once notifications are allowed.
            store.setNotified(plan.notified - plan.toNotify.map { it.key }.toSet())
        }
        return Result.success()
    }
}
