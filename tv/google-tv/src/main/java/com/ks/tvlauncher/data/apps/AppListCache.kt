package com.ks.tvlauncher.data.apps

import android.content.Context
import com.ks.tvlauncher.domain.AppInfo
import com.ks.tvlauncher.domain.AppListCodec
import java.io.File
import java.io.IOException

/**
 * The last app list in KS's own storage (`files/app_list.json`), so a cold start can show Home at
 * once. Names of installed apps only; stays on the TV and is never backed up (allowBackup is off).
 * Call from a background thread.
 */
class AppListCache(context: Context) {
    private val file = File(context.applicationContext.filesDir, "app_list.json")

    /** The cached list, or null when there is none or it cannot be read. */
    fun read(): List<AppInfo>? = try {
        if (file.isFile) AppListCodec.decode(file.readText()) else null
    } catch (e: IOException) {
        null
    }

    /** Writes [apps] atomically (a temporary file renamed over the old one). Failures are ignored. */
    fun write(apps: List<AppInfo>) {
        try {
            val temp = File(file.parentFile, file.name + ".tmp")
            temp.writeText(AppListCodec.encode(apps))
            if (!temp.renameTo(file)) temp.delete()
        } catch (e: IOException) {
            // A missing cache only means the next cold start waits for the scan, as before.
        }
    }
}
