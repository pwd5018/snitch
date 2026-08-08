package com.pwd5018.snitch.di

import android.content.Context
import com.pwd5018.snitch.audit.AuditRepository
import com.pwd5018.snitch.audit.PackageScanner
import com.pwd5018.snitch.data.db.AppDatabase
import com.pwd5018.snitch.vpn.VpnStatusRepository

/**
 * Manual DI container — a single Gradle module for a solo, personal app doesn't justify the
 * extra KSP annotation-processing cost of Hilt. Revisit if the module/feature count grows.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getInstance(appContext)
    private val packageScanner = PackageScanner(appContext.packageManager)

    val auditRepository: AuditRepository by lazy {
        AuditRepository(
            database = database,
            installedAppDao = database.installedAppDao(),
            permissionGrantDao = database.permissionGrantDao(),
            riskFlagDao = database.riskFlagDao(),
            packageScanner = packageScanner,
        )
    }

    val vpnStatusRepository: VpnStatusRepository by lazy {
        VpnStatusRepository(appContext)
    }
}
