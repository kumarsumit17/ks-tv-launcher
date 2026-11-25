package com.ks.tvlauncher.data.system

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

enum class NetworkKind { WIFI, ETHERNET, OTHER, OFFLINE }

@Immutable
data class NetworkStatus(
    val kind: NetworkKind,
    /** Only when Android lets an app without location access read it; usually null. */
    val ssid: String? = null,
)

@Immutable
data class BluetoothStatus(
    /** False when the TV has no Bluetooth. */
    val available: Boolean,
    val enabled: Boolean,
    /** Paired devices; null unless Bluetooth permission is already granted (KS never asks). */
    val pairedCount: Int?,
)

/**
 * Wi-Fi and Bluetooth status for the Control Center tiles. Read only: a normal app cannot
 * switch radios. Callbacks and receivers exist only while a flow is collected.
 */
class SystemStatusRepository(private val context: Context) {

    val network: Flow<NetworkStatus> = callbackFlow {
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        if (connectivity == null) {
            trySend(NetworkStatus(NetworkKind.OFFLINE))
            awaitClose { }
            return@callbackFlow
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(readNetwork(connectivity))
            }

            override fun onLost(network: Network) {
                trySend(readNetwork(connectivity))
            }
        }
        trySend(readNetwork(connectivity))
        connectivity.registerDefaultNetworkCallback(callback)
        awaitClose { connectivity.unregisterNetworkCallback(callback) }
    }.conflate().distinctUntilChanged()

    val bluetooth: Flow<BluetoothStatus> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(readBluetooth())
            }
        }
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        trySend(readBluetooth())
        awaitClose { context.unregisterReceiver(receiver) }
    }.conflate().distinctUntilChanged()

    private fun readNetwork(connectivity: ConnectivityManager): NetworkStatus {
        val capabilities = connectivity.activeNetwork?.let { connectivity.getNetworkCapabilities(it) }
            ?: return NetworkStatus(NetworkKind.OFFLINE)
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus(NetworkKind.WIFI, ssid(capabilities))
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkStatus(NetworkKind.ETHERNET)
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkStatus(NetworkKind.OTHER)
            else -> NetworkStatus(NetworkKind.OFFLINE)
        }
    }

    /** Android hides the SSID ("<unknown ssid>") from apps without location access; then null. */
    private fun ssid(capabilities: NetworkCapabilities): String? {
        val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (capabilities.transportInfo as? WifiInfo)?.ssid
        } else {
            @Suppress("DEPRECATION")
            context.applicationContext.getSystemService(WifiManager::class.java)?.connectionInfo?.ssid
        }
        return raw?.removeSurrounding("\"")?.takeIf { it.isNotBlank() && it != UNKNOWN_SSID }
    }

    private fun readBluetooth(): BluetoothStatus {
        val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
            ?: return BluetoothStatus(available = false, enabled = false, pairedCount = null)
        val enabled = try {
            adapter.isEnabled
        } catch (e: SecurityException) {
            false
        }
        return BluetoothStatus(available = true, enabled = enabled, pairedCount = if (enabled) pairedCount(adapter) else null)
    }

    // Guarded by the explicit grant check below; KS never requests the permission itself.
    @SuppressLint("MissingPermission")
    private fun pairedCount(adapter: BluetoothAdapter): Int? {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
        if (!granted) return null
        return try {
            adapter.bondedDevices?.size
        } catch (e: SecurityException) {
            null
        }
    }

    private companion object {
        /** What Android returns instead of the SSID to apps without location access. */
        const val UNKNOWN_SSID = "<unknown ssid>"
    }
}
