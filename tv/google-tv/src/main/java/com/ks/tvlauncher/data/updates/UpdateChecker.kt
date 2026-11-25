package com.ks.tvlauncher.data.updates

import com.ks.tvlauncher.domain.AppUpdates
import com.ks.tvlauncher.domain.ReleaseInfo
import java.io.IOException

/** What one update check found. */
sealed interface CheckResult {
    data object UpToDate : CheckResult
    data class Available(val release: ReleaseInfo) : CheckResult
    /** GitHub answered 403/429 (now or earlier today): no request until tomorrow. */
    data object Waiting : CheckResult
    data object Failed : CheckResult
}

/**
 * One check against [source], shared by the daily work and "Check for updates". Remembers a
 * newer release in [store]; the daily check also notifies it once per version.
 */
class UpdateChecker(
    private val source: UpdateSource,
    private val store: UpdateStore,
    private val installedCode: Int,
    private val today: () -> Long,
    /** Posts the notification; false when notifications are blocked (try again next time). */
    private val notify: (ReleaseInfo) -> Boolean,
) {
    suspend fun check(automatic: Boolean): CheckResult {
        if (today() < store.waitUntilDay()) return CheckResult.Waiting
        val release = try {
            source.latest()
        } catch (e: RateLimitedException) {
            store.setWaitUntilDay(today() + 1)
            return CheckResult.Waiting
        } catch (e: IOException) {
            return CheckResult.Failed
        }
        if (!AppUpdates.isNewer(release.versionCode, installedCode)) {
            store.setAvailable(null)
            return CheckResult.UpToDate
        }
        store.setAvailable(release)
        if (automatic && store.notifiedCode() != release.versionCode && notify(release)) {
            store.setNotifiedCode(release.versionCode)
        }
        return CheckResult.Available(release)
    }

    /**
     * After an update (or a manual install) the pending release may already be installed:
     * forget it. True when something was cleared.
     */
    suspend fun clearIfInstalled(pending: ReleaseInfo?): Boolean {
        if (pending == null || AppUpdates.isNewer(pending.versionCode, installedCode)) return false
        store.setAvailable(null)
        return true
    }
}
