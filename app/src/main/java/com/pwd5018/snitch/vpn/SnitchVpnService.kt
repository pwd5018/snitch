package com.pwd5018.snitch.vpn

import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.IOException

/**
 * Round-1 skeleton: establishes the tunnel and proves the packet read loop works. It does
 * NOT forward, proxy, or decrypt traffic, and does NOT claim the default route (0.0.0.0/0) —
 * it only routes an unused test subnet, so a bug here can't blackhole this device's real
 * connectivity before a proxy exists to forward packets onward.
 *
 * TODO(round2): add the local TLS-decrypting proxy, claim the default route, and wire up
 * ConnectivityManager.getConnectionOwnerUid to map connections back to owning apps.
 */
class SnitchVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var serviceScope: CoroutineScope? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> connect()
            ACTION_DISCONNECT -> disconnect()
        }
        // Redelivery after a process kill would arrive with intent == null and no consent
        // context to resume the tunnel with — not sticky, the user has to re-trigger connect.
        return START_NOT_STICKY
    }

    private fun connect() {
        if (vpnInterface != null) return

        _state.value = VpnState.Starting

        val builder = Builder()
            .setSession(SESSION_NAME)
            .addAddress(TUNNEL_ADDRESS, TUNNEL_PREFIX)
            // Narrow, unused test subnet only — NOT 0.0.0.0/0, see class KDoc. If 10.111.0.0/24
            // happens to collide with your real LAN, change TEST_SUBNET to something that doesn't.
            .addRoute(TEST_SUBNET, TEST_SUBNET_PREFIX)
            .setMtu(MTU)

        // establish() returns null (rather than throwing) when VPN consent hasn't been granted —
        // check the tunnel before touching the foreground notification.
        val established = builder.establish()
        if (established == null) {
            _state.value = VpnState.Error("VPN permission not granted")
            stopSelf()
            return
        }

        VpnNotification.createChannel(this)
        try {
            startForeground(
                VpnNotification.NOTIFICATION_ID,
                VpnNotification.build(this),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED,
            )
        } catch (e: IllegalStateException) {
            // e.g. ForegroundServiceTypeNotAllowedException if consent was revoked between the
            // UI check and here — tear down the tunnel we just opened rather than leak it.
            Log.e(TAG, "startForeground rejected", e)
            established.close()
            _state.value = VpnState.Error("Foreground service start rejected: ${e.message}")
            stopSelf()
            return
        }

        vpnInterface = established
        _state.value = VpnState.Running

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        serviceScope = scope
        scope.launch { readLoop(established) }
    }

    private fun readLoop(pfd: ParcelFileDescriptor) {
        val input = FileInputStream(pfd.fileDescriptor)
        val buffer = ByteArray(MTU)
        var packetCount = 0L
        try {
            while (vpnInterface != null) {
                val length = input.read(buffer)
                if (length > 0) {
                    packetCount++
                    if (packetCount % LOG_EVERY_N_PACKETS == 0L) {
                        Log.d(TAG, "Read $packetCount packets so far (round-1 skeleton: not forwarded)")
                    }
                }
            }
        } catch (e: IOException) {
            Log.d(TAG, "Read loop stopped: ${e.message}")
        }
    }

    private fun disconnect() {
        serviceScope?.cancel()
        serviceScope = null

        vpnInterface?.let {
            try {
                it.close()
            } catch (e: IOException) {
                Log.w(TAG, "Error closing VPN interface", e)
            }
        }
        vpnInterface = null

        _state.value = VpnState.Stopped
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    override fun onRevoke() {
        // System revoked VPN permission (e.g. user picked another always-on VPN) — tear down cleanly.
        disconnect()
        super.onRevoke()
    }

    companion object {
        private const val TAG = "SnitchVpnService"
        const val ACTION_CONNECT = "com.pwd5018.snitch.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.pwd5018.snitch.vpn.DISCONNECT"

        private const val SESSION_NAME = "Snitch"
        private const val TUNNEL_ADDRESS = "10.111.0.1"
        private const val TUNNEL_PREFIX = 24
        private const val TEST_SUBNET = "10.111.0.0"
        private const val TEST_SUBNET_PREFIX = 24
        private const val MTU = 1500
        private const val LOG_EVERY_N_PACKETS = 50L

        private val _state = MutableStateFlow<VpnState>(VpnState.Stopped)
        val state: StateFlow<VpnState> = _state.asStateFlow()
    }
}
