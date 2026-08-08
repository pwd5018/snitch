package com.pwd5018.snitch.vpn

sealed interface VpnState {
    data object Stopped : VpnState
    data object Starting : VpnState
    data object Running : VpnState
    data class Error(val message: String) : VpnState
}
