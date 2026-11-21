package com.ks.tvlauncher.domain

/** KS's two daily WorkManager jobs. Each is unique work of its own; neither cancels the other. */
enum class DailyWork { RENEWAL_REMINDERS, UPDATE_CHECK }

/**
 * Which daily work to re-register shortly after Home is first shown. A force-stop makes Android
 * drop an app's scheduled jobs, and KS starts WorkManager only on demand, so without this the
 * jobs stayed gone until the next TV restart or a change to either setting.
 *
 * Registration uses the KEEP policy, so work that is already scheduled is left as it is.
 */
object DailyWorkPlan {
    fun toRegister(remindersEnabled: Boolean, autoUpdateCheck: Boolean, inAppUpdates: Boolean): Set<DailyWork> =
        buildSet {
            if (remindersEnabled) add(DailyWork.RENEWAL_REMINDERS)
            // Debug and perf builds never check for updates (specs/dev-workflow.md).
            if (inAppUpdates && autoUpdateCheck) add(DailyWork.UPDATE_CHECK)
        }
}
