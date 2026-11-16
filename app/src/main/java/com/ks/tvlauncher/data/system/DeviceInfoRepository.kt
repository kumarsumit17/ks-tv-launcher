package com.ks.tvlauncher.data.system

import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.ks.tvlauncher.domain.DeviceNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

data class DeviceInfo(
    /** The name from Android Settings › Device name; null if the TV has none. */
    val systemName: String?,
    /** Tidied manufacturer + model, e.g. "Google Chromecast". */
    val model: String,
    /** e.g. "12". */
    val androidRelease: String,
    val sdkInt: Int,
)

/** Reads what the TV says about itself. Needs no permission. */
class DeviceInfoRepository(private val context: Context) {

    /**
     * Emits the current values, then again only if the user renames the TV in Android
     * settings. Driven by a content observer that exists while the flow is collected,
     * so nothing is polled.
     */
    val deviceInfo: Flow<DeviceInfo> = callbackFlow {
        val resolver = context.contentResolver
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(read())
            }
        }
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.DEVICE_NAME), false, observer)
        trySend(read())
        awaitClose { resolver.unregisterContentObserver(observer) }
    }
        .conflate()
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    private fun read(): DeviceInfo = DeviceInfo(
        systemName = runCatching {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        }.getOrNull()?.trim()?.takeIf { it.isNotEmpty() },
        model = DeviceNames.tidyModel(Build.MANUFACTURER, Build.MODEL),
        androidRelease = Build.VERSION.RELEASE.orEmpty(),
        sdkInt = Build.VERSION.SDK_INT,
    )
}
