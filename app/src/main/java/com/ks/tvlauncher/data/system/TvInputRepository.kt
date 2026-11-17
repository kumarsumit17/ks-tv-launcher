package com.ks.tvlauncher.data.system

import android.content.Context
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.os.Handler
import android.os.Looper
import com.ks.tvlauncher.domain.InputConnection
import com.ks.tvlauncher.domain.InputKind
import com.ks.tvlauncher.domain.TvInput
import com.ks.tvlauncher.domain.TvInputSource
import com.ks.tvlauncher.domain.TvInputs
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * The TV's HDMI and other passthrough inputs, for the Control Center Inputs tile. The
 * `TvInputCallback` is registered only while [inputs] is collected (no polling). Needs no
 * permission. On a device without TV input support the list is empty.
 */
class TvInputRepository(private val context: Context) {

    val inputs: Flow<List<TvInput>> = callbackFlow {
        val manager = try {
            context.getSystemService(TvInputManager::class.java)
        } catch (e: RuntimeException) {
            null
        }
        if (manager == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val callback = object : TvInputManager.TvInputCallback() {
            override fun onInputAdded(inputId: String) {
                trySend(read(manager))
            }

            override fun onInputRemoved(inputId: String) {
                trySend(read(manager))
            }

            override fun onInputUpdated(inputId: String) {
                trySend(read(manager))
            }

            override fun onInputStateChanged(inputId: String, state: Int) {
                trySend(read(manager))
            }

            override fun onTvInputInfoUpdated(inputInfo: TvInputInfo) {
                trySend(read(manager))
            }
        }
        trySend(read(manager))
        manager.registerCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { manager.unregisterCallback(callback) }
    }.conflate().distinctUntilChanged()

    private fun read(manager: TvInputManager): List<TvInput> {
        val infos = try {
            manager.tvInputList
        } catch (e: RuntimeException) {
            emptyList()
        }
        return TvInputs.select(
            infos.map { info ->
                TvInputSource(
                    id = info.id,
                    kind = kindOf(info.type),
                    passthrough = info.isPassthroughInput,
                    customLabel = info.loadCustomLabel(context)?.toString(),
                    label = info.loadLabel(context)?.toString(),
                    connection = when (manager.getInputState(info.id)) {
                        TvInputManager.INPUT_STATE_CONNECTED -> InputConnection.CONNECTED
                        TvInputManager.INPUT_STATE_CONNECTED_STANDBY -> InputConnection.STANDBY
                        else -> InputConnection.DISCONNECTED
                    },
                )
            },
        )
    }

    private fun kindOf(type: Int): InputKind = when (type) {
        TvInputInfo.TYPE_HDMI -> InputKind.HDMI
        TvInputInfo.TYPE_DISPLAY_PORT -> InputKind.DISPLAY_PORT
        TvInputInfo.TYPE_DVI -> InputKind.DVI
        TvInputInfo.TYPE_COMPONENT -> InputKind.COMPONENT
        TvInputInfo.TYPE_COMPOSITE -> InputKind.COMPOSITE
        TvInputInfo.TYPE_SVIDEO -> InputKind.SVIDEO
        TvInputInfo.TYPE_SCART -> InputKind.SCART
        TvInputInfo.TYPE_VGA -> InputKind.VGA
        else -> InputKind.OTHER
    }

    companion object {
        /** Opens the system's player for the input, which switches the TV to it. */
        fun switchIntent(inputId: String): Intent =
            Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(inputId))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
