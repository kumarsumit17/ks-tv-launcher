package com.ks.tvlauncher.ui.controlcenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ks.tvlauncher.KsApplication
import com.ks.tvlauncher.data.system.BluetoothStatus
import com.ks.tvlauncher.data.system.NetworkStatus
import com.ks.tvlauncher.data.system.SystemStatusRepository
import com.ks.tvlauncher.data.system.TvInputRepository
import com.ks.tvlauncher.domain.TvInput
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Live Wi-Fi, Bluetooth and TV input status while Control Center is open. */
class ControlCenterViewModel(
    repository: SystemStatusRepository,
    tvInputs: TvInputRepository,
) : ViewModel() {

    val network: StateFlow<NetworkStatus?> =
        repository.network.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), null)

    val bluetooth: StateFlow<BluetoothStatus?> =
        repository.bluetooth.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), null)

    /** Passthrough inputs (HDMI, AV); null until first read. Empty hides the Inputs tile. */
    val inputs: StateFlow<List<TvInput>?> =
        tvInputs.inputs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), null)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as KsApplication
                ControlCenterViewModel(app.container.systemStatusRepository, app.container.tvInputRepository)
            }
        }
    }
}
