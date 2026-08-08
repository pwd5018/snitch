package com.pwd5018.snitch.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pwd5018.snitch.data.db.relation.AppWithGrantsAndFlags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuditUiState(
    val apps: List<AppWithGrantsAndFlags> = emptyList(),
    val isScanning: Boolean = false,
)

class AuditViewModel(private val repository: AuditRepository) : ViewModel() {

    private val isScanning = MutableStateFlow(false)

    val uiState: StateFlow<AuditUiState> =
        combine(repository.apps, isScanning) { apps, scanning ->
            AuditUiState(apps = apps, isScanning = scanning)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuditUiState())

    init {
        rescan()
    }

    fun rescan() {
        viewModelScope.launch {
            isScanning.value = true
            repository.rescan()
            isScanning.value = false
        }
    }

    class Factory(private val repository: AuditRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuditViewModel(repository) as T
    }
}
