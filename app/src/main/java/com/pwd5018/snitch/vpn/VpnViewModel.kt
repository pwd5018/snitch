package com.pwd5018.snitch.vpn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.StateFlow

class VpnViewModel(private val repository: VpnStatusRepository) : ViewModel() {
    val state: StateFlow<VpnState> = repository.state

    fun connect() = repository.connect()
    fun disconnect() = repository.disconnect()

    class Factory(private val repository: VpnStatusRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = VpnViewModel(repository) as T
    }
}
