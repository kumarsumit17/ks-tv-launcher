package com.ks.tvlauncher.data.updates

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ks.tvlauncher.BuildConfig
import com.ks.tvlauncher.KsApplication
import com.ks.tvlauncher.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * The daily update check: its own unique WorkManager work, on an unmetered network. Separate
 * from renewal reminders: neither ever cancels the other's work. Does nothing in debug or perf
 * builds ([BuildConfig.IN_APP_UPDATES]).
 */
class UpdateScheduler(
    context: Context,
    private val settings: SettingsRepository,
    private val store: UpdateStore,
) {
    private val context = context.applicationContext
    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    /** "Check for updates automatically" switched on or off. */
    fun setEnabled(enabled: Boolean) {
        if (!BuildConfig.IN_APP_UPDATES) return
        if (enabled) {
            val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(1, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build())
                .build()
            workManager.enqueueUniquePeriodicWork(DAILY, ExistingPeriodicWorkPolicy.KEEP, request)
        } else {
            workManager.cancelUniqueWork(DAILY)
        }
    }

    /**
     * Schedules the daily check once per installed version (the switch is on by default, so
     * it must start without a visit to Settings). Called well after Home has shown, never in
     * `Application.onCreate`; later starts of the same version do not touch WorkManager.
     */
    suspend fun ensureScheduled() {
        if (!BuildConfig.IN_APP_UPDATES) return
        if (store.scheduledFor() == BuildConfig.VERSION_CODE) return
        setEnabled(settings.settings.first().autoUpdateCheck)
        store.setScheduledFor(BuildConfig.VERSION_CODE)
    }

    private companion object {
        const val DAILY = "app_update_check_daily"
    }
}

/** The daily check. Skips when the switch is off; WorkManager decides the exact time. */
class UpdateCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!BuildConfig.IN_APP_UPDATES) return Result.success()
        val container = (applicationContext as KsApplication).container
        if (!container.settingsRepository.settings.first().autoUpdateCheck) return Result.success()
        container.updateChecker.check(automatic = true)
        // A failed check simply waits for tomorrow's run; no retries hammer the network.
        return Result.success()
    }
}
