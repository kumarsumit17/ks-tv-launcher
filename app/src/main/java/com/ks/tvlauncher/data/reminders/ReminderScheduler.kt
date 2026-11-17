package com.ks.tvlauncher.data.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ks.tvlauncher.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Schedules [RenewalReminderWorker]: one unique daily job while reminders are on, plus a
 * one-off check after every change. WorkManager starts only when this is first used, never at
 * app start.
 */
class ReminderScheduler(context: Context, private val settings: SettingsRepository) {
    private val context = context.applicationContext
    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    /** Reminders switched on or off in Settings. */
    fun setEnabled(enabled: Boolean) {
        if (enabled) {
            workManager.enqueueUniquePeriodicWork(
                DAILY,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<RenewalReminderWorker>(1, TimeUnit.DAYS).build(),
            )
        } else {
            workManager.cancelUniqueWork(DAILY)
        }
        // Runs the check now: notifies what is due, or withdraws everything when switched off.
        enqueueCheck()
    }

    /** A subscription or the lead time changed: re-check soon, if reminders are on. */
    suspend fun onChanged() {
        if (settings.settings.first().remindersEnabled) enqueueCheck()
    }

    private fun enqueueCheck() {
        workManager.enqueueUniqueWork(
            NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<RenewalReminderWorker>().build(),
        )
    }

    private companion object {
        const val DAILY = "renewal_reminders_daily"
        const val NOW = "renewal_reminders_now"
    }
}
