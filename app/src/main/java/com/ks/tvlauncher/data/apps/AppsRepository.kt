package com.ks.tvlauncher.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import androidx.core.content.pm.PackageInfoCompat
import com.ks.tvlauncher.domain.AppCategory
import com.ks.tvlauncher.domain.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import java.text.Collator

/**
 * Installed, launchable apps. No polling: the list is re-read when `LauncherApps` reports an
 * install, update or removal, and the callback is registered only while someone collects.
 */
class AppsRepository(context: Context, scope: CoroutineScope) {

    private val appContext = context.applicationContext
    private val pm: PackageManager = appContext.packageManager
    private val launcherApps: LauncherApps? = appContext.getSystemService(LauncherApps::class.java)
    private val manualRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * Null until the first scan finishes, so callers can tell "not loaded yet" from "no apps".
     * Stops listening 5 s after the last collector leaves, and keeps the last list.
     */
    val apps: StateFlow<List<AppInfo>?> = merge(packageChanges(), manualRefresh)
        .conflate()
        .map { queryApps() }
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    /** Re-reads the app list, for example after a launch failed because an app had gone. */
    fun refresh() {
        manualRefresh.tryEmit(Unit)
    }

    /** The leanback launch intent, else the normal one; null if the app can't be launched. */
    fun launchIntent(packageName: String): Intent? =
        (pm.getLeanbackLaunchIntentForPackage(packageName) ?: pm.getLaunchIntentForPackage(packageName))
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private fun packageChanges(): Flow<Unit> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                trySend(Unit)
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
                trySend(Unit)
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                trySend(Unit)
            }

            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                trySend(Unit)
            }

            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                trySend(Unit)
            }
        }
        launcherApps?.registerCallback(callback, Handler(Looper.getMainLooper()))
        trySend(Unit)
        awaitClose { launcherApps?.unregisterCallback(callback) }
    }

    private fun queryApps(): List<AppInfo> {
        // Leanback entries win: their activity is the TV one and usually has a banner.
        val byPackage = LinkedHashMap<String, ResolveInfo>()
        for (info in query(Intent.CATEGORY_LEANBACK_LAUNCHER)) byPackage.putIfAbsent(info.activityInfo.packageName, info)
        for (info in query(Intent.CATEGORY_LAUNCHER)) byPackage.putIfAbsent(info.activityInfo.packageName, info)
        byPackage.remove(appContext.packageName)

        val collator = Collator.getInstance()
        return byPackage.values
            .map(::toAppInfo)
            .sortedWith(compareBy(collator) { app: AppInfo -> app.label })
    }

    private fun query(category: String): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
    }

    private fun toAppInfo(info: ResolveInfo): AppInfo {
        val activity = info.activityInfo
        val application = activity.applicationInfo
        val versionCode = try {
            PackageInfoCompat.getLongVersionCode(pm.getPackageInfo(activity.packageName, 0))
        } catch (e: PackageManager.NameNotFoundException) {
            0L
        }
        return AppInfo(
            packageName = activity.packageName,
            activityName = activity.name,
            label = info.loadLabel(pm).toString().trim().ifEmpty { activity.packageName },
            versionCode = versionCode,
            category = categoryOf(application),
            // Resource IDs only: checking for a banner decodes nothing.
            hasBanner = activity.banner != 0 || application.banner != 0,
        )
    }

    companion object {
        /** Maps `ApplicationInfo.category` to the drawer's filter; other categories map to null. */
        fun categoryOf(application: ApplicationInfo): AppCategory? = categoryOf(application.category)

        fun categoryOf(category: Int): AppCategory? = when (category) {
            ApplicationInfo.CATEGORY_VIDEO -> AppCategory.VIDEO
            ApplicationInfo.CATEGORY_AUDIO -> AppCategory.MUSIC
            ApplicationInfo.CATEGORY_GAME -> AppCategory.GAMES
            ApplicationInfo.CATEGORY_PRODUCTIVITY, ApplicationInfo.CATEGORY_ACCESSIBILITY -> AppCategory.TOOLS
            else -> null
        }
    }
}
