package com.ks.tvlauncher.data.system

import android.Manifest
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.storage.StorageManager
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException

/** Apps (code, data and cache of every app) and everything else that is used. */
@Immutable
data class StorageBreakdown(val appsBytes: Long, val otherBytes: Long)

@Immutable
data class DeviceStats(
    val ramTotal: Long,
    val ramAvailable: Long,
    val storageTotal: Long,
    val storageAvailable: Long,
    /** Only with Usage access granted and when asked for; otherwise null. */
    val breakdown: StorageBreakdown? = null,
) {
    val ramUsed: Long get() = (ramTotal - ramAvailable).coerceAtLeast(0L)
    val storageUsed: Long get() = (storageTotal - storageAvailable).coerceAtLeast(0L)
}

/** RAM and internal storage, read with no permission. */
class DeviceStatsRepository(private val context: Context) {

    /**
     * Emits now and then every 10 s while collected. Collect it only while Home is on screen
     * (the view model shares it with `WhileSubscribed`, the UI with lifecycle), so nothing runs
     * in the background. [withBreakdown] adds the Apps / Other split when Usage access is granted.
     */
    fun stats(withBreakdown: Boolean): Flow<DeviceStats> = flow {
        while (true) {
            emit(read(withBreakdown))
            delay(REFRESH_MS)
        }
    }.flowOn(Dispatchers.IO)

    /** True once the user has allowed Usage access for KS in Android settings. */
    fun usageAccessGranted(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            context.checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

    private fun read(withBreakdown: Boolean): DeviceStats {
        val memory = ActivityManager.MemoryInfo()
        context.getSystemService(ActivityManager::class.java)?.getMemoryInfo(memory)
        val statFs = StatFs(Environment.getDataDirectory().path)
        val total = statFs.totalBytes
        val available = statFs.availableBytes
        return DeviceStats(
            ramTotal = memory.totalMem,
            ramAvailable = memory.availMem,
            storageTotal = total,
            storageAvailable = available,
            breakdown = if (withBreakdown) breakdown(total - available) else null,
        )
    }

    private fun breakdown(used: Long): StorageBreakdown? {
        if (!usageAccessGranted()) return null
        val manager = context.getSystemService(StorageStatsManager::class.java) ?: return null
        return try {
            val stats = manager.queryStatsForUser(StorageManager.UUID_DEFAULT, Process.myUserHandle())
            val apps = stats.appBytes + stats.dataBytes + stats.cacheBytes
            StorageBreakdown(appsBytes = apps.coerceAtMost(used), otherBytes = (used - apps).coerceAtLeast(0L))
        } catch (e: IOException) {
            null
        } catch (e: SecurityException) {
            null
        }
    }

    private companion object {
        const val REFRESH_MS = 10_000L
    }
}
