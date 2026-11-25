package com.ks.tvlauncher.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import java.time.ZonedDateTime

/**
 * Emits the current time immediately, then once at every minute boundary, and whenever the
 * clock, time zone or locale changes.
 *
 * It rides on the system's own `ACTION_TIME_TICK` broadcast instead of a timer, so the app
 * never wakes itself up to poll the clock. The receiver is registered at runtime only while
 * the flow is collected (there is no manifest receiver), and is removed when collection stops.
 */
fun minuteTicker(context: Context): Flow<ZonedDateTime> = callbackFlow {
    val appContext = context.applicationContext
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySend(ZonedDateTime.now())
        }
    }
    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_TIME_TICK)
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
        addAction(Intent.ACTION_LOCALE_CHANGED)
    }
    ContextCompat.registerReceiver(appContext, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    trySend(ZonedDateTime.now())
    awaitClose { appContext.unregisterReceiver(receiver) }
}.conflate()
