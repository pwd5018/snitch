package com.pwd5018.snitch.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService

object VpnPermissionHelper {
    /** Non-null when the system VPN consent dialog still needs to be shown; null if already granted. */
    fun prepareIntent(context: Context): Intent? = VpnService.prepare(context)
}
