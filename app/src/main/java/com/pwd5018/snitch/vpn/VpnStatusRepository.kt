package com.pwd5018.snitch.vpn

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.StateFlow

class VpnStatusRepository(private val context: Context) {
    val state: StateFlow<VpnState> = SnitchVpnService.state

    fun connect() {
        val intent = Intent(context, SnitchVpnService::class.java).setAction(SnitchVpnService.ACTION_CONNECT)
        context.startForegroundService(intent)
    }

    fun disconnect() {
        val intent = Intent(context, SnitchVpnService::class.java).setAction(SnitchVpnService.ACTION_DISCONNECT)
        context.startService(intent)
    }
}
