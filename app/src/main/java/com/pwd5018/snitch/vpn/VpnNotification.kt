package com.pwd5018.snitch.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pwd5018.snitch.R

object VpnNotification {
    const val CHANNEL_ID = "vpn_status"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "VPN status",
            NotificationManager.IMPORTANCE_LOW,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun build(context: Context): Notification {
        val disconnectIntent = Intent(context, SnitchVpnService::class.java)
            .setAction(SnitchVpnService.ACTION_DISCONNECT)
        val disconnectPendingIntent = PendingIntent.getService(
            context,
            0,
            disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Snitch tunnel active")
            .setContentText("Traffic Inspector VPN is running (test subnet only, round 1)")
            .setSmallIcon(R.drawable.ic_notification_shield)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(0, "Disconnect", disconnectPendingIntent)
            .build()
    }
}
