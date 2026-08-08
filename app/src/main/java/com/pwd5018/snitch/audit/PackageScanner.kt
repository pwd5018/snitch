package com.pwd5018.snitch.audit

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import com.pwd5018.snitch.data.db.entity.InstalledAppEntity
import com.pwd5018.snitch.data.db.entity.PermissionGrantEntity
import com.pwd5018.snitch.data.db.entity.ProtectionLevel

/**
 * Reads every installed app via PackageManager. Requires QUERY_ALL_PACKAGES (declared in the
 * manifest) — without it, getInstalledPackages() silently filters to package-visibility-exempt
 * apps only on API 30+.
 */
class PackageScanner(private val packageManager: PackageManager) {

    data class ScannedApp(
        val app: InstalledAppEntity,
        val grants: List<PermissionGrantEntity>,
    )

    // Lives for as long as this PackageScanner instance (one per process, held by AppContainer):
    // many permissions (INTERNET, ACCESS_FINE_LOCATION, ...) repeat across dozens of apps and
    // across rescans, and a permission's protection level essentially never changes at runtime,
    // so caching across scans (not just within one) is intentional, not just incidental.
    private val protectionLevelCache = mutableMapOf<String, String>()

    fun scanInstalledApps(): List<ScannedApp> {
        val flags = PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
        val packages = packageManager.getInstalledPackages(flags)
        val scannedAt = System.currentTimeMillis()
        return packages.map { toScannedApp(it, scannedAt) }
    }

    private fun toScannedApp(packageInfo: PackageInfo, scannedAt: Long): ScannedApp {
        val applicationInfo = packageInfo.applicationInfo
        val app = InstalledAppEntity(
            packageName = packageInfo.packageName,
            appLabel = applicationInfo?.loadLabel(packageManager)?.toString() ?: packageInfo.packageName,
            uid = applicationInfo?.uid ?: -1,
            versionName = packageInfo.versionName,
            versionCode = packageInfo.longVersionCode,
            targetSdkVersion = applicationInfo?.targetSdkVersion ?: 0,
            isSystemApp = (applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0,
            firstInstallTime = packageInfo.firstInstallTime,
            lastUpdateTime = packageInfo.lastUpdateTime,
            lastScannedAt = scannedAt,
        )
        return ScannedApp(app, requestedPermissionGrants(packageInfo))
    }

    private fun requestedPermissionGrants(packageInfo: PackageInfo): List<PermissionGrantEntity> {
        val names = packageInfo.requestedPermissions ?: return emptyList()
        val grantFlags = packageInfo.requestedPermissionsFlags ?: IntArray(names.size)
        return names.mapIndexed { index, permissionName ->
            val isGranted = (grantFlags.getOrElse(index) { 0 } and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            PermissionGrantEntity(
                packageName = packageInfo.packageName,
                permissionName = permissionName,
                protectionLevel = protectionLevelOf(permissionName),
                isGranted = isGranted,
            )
        }
    }

    private fun protectionLevelOf(permissionName: String): String =
        protectionLevelCache.getOrPut(permissionName) {
            try {
                val info = packageManager.getPermissionInfo(permissionName, 0)
                when (info.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE) {
                    PermissionInfo.PROTECTION_NORMAL -> ProtectionLevel.NORMAL
                    PermissionInfo.PROTECTION_DANGEROUS -> ProtectionLevel.DANGEROUS
                    PermissionInfo.PROTECTION_SIGNATURE -> ProtectionLevel.SIGNATURE
                    else -> ProtectionLevel.SPECIAL
                }
            } catch (e: PackageManager.NameNotFoundException) {
                ProtectionLevel.UNKNOWN
            }
        }
}
