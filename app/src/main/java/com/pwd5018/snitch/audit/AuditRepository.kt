package com.pwd5018.snitch.audit

import androidx.room.withTransaction
import com.pwd5018.snitch.data.db.AppDatabase
import com.pwd5018.snitch.data.db.dao.InstalledAppDao
import com.pwd5018.snitch.data.db.dao.PermissionGrantDao
import com.pwd5018.snitch.data.db.dao.RiskFlagDao
import com.pwd5018.snitch.data.db.relation.AppWithGrantsAndFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuditRepository(
    private val database: AppDatabase,
    private val installedAppDao: InstalledAppDao,
    private val permissionGrantDao: PermissionGrantDao,
    private val riskFlagDao: RiskFlagDao,
    private val packageScanner: PackageScanner,
    private val rules: List<AuditRule> = RiskRules.all,
) {
    val apps: Flow<List<AppWithGrantsAndFlags>> = installedAppDao.observeAllWithDetails()

    // Wrapped in one transaction so observers see a single atomic update at the end of the
    // scan instead of a per-package flicker (each grants/flags delete+reinsert would otherwise
    // emit its own intermediate Flow value), and so an interrupted scan can't leave rows missing.
    suspend fun rescan() = withContext(Dispatchers.IO) {
        // Scan (a PackageManager IPC round-trip) happens outside the transaction so it doesn't
        // hold a DB transaction open while waiting on the system service.
        val scanned = packageScanner.scanInstalledApps()
        val now = System.currentTimeMillis()

        database.withTransaction {
            for (entry in scanned) {
                installedAppDao.upsert(entry.app)

                permissionGrantDao.deleteForPackage(entry.app.packageName)
                permissionGrantDao.insertAll(entry.grants)

                val flags = rules.mapNotNull { it.evaluate(entry.app.packageName, entry.grants, now) }
                riskFlagDao.deleteForPackage(entry.app.packageName)
                riskFlagDao.insertAll(flags)
            }

            installedAppDao.deleteMissing(scanned.map { it.app.packageName })
        }
    }
}
