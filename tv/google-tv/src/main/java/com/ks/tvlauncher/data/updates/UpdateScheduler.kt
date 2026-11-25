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
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * The daily update check: its own unique WorkManager work, on an unmetered network. Separate
 * from renewal reminders: neither ever cancels the other's work. Does nothing in debug or perf
 * builds ([BuildConfig.IN_APP_UPDATES]).
 */
class UpdateScheduler(context: Context) {
    private val context = context.applicationContext
    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    /** "Check for updates automatically" switched on or off. */
    fun setEnabled(enabled: Boolean) {
        if (!BuildConfig.IN_APP_UPDATES) return
        if (enabled) enqueueDaily() else workManager.cancelUniqueWork(DAILY)
    }

    /**
     * Re-registers the daily check after start (KEEP: an existing schedule is left alone). The
     * switch is on by default, so this also starts the check without a visit to Settings, and it
     * brings the job back after a force-stop, which makes Android drop it. Called about 5 s after
     * Home is first shown, only while the switch is on; never in `Application.onCreate`.
     */
    fun ensureDaily() {
        if (BuildConfig.IN_APP_UPDATES) enqueueDaily()
    }

    private fun enqueueDaily() {
        val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build())
            .build()
        workManager.enqueueUniquePeriodicWork(DAILY, ExistingPeriodicWorkPolicy.KEEP, request)
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
