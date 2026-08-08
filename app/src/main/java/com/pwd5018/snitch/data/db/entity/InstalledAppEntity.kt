package com.pwd5018.snitch.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * [uid] is indexed (not the primary key) because Traffic Inspector will later join
 * ConnectivityManager.getConnectionOwnerUid results back to this table via uid, while
 * [packageName] stays the natural key the audit UI and Room relations key off of.
 */
@Entity(tableName = "installed_apps", indices = [Index("uid")])
data class InstalledAppEntity(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val uid: Int,
    val versionName: String?,
    val versionCode: Long,
    val targetSdkVersion: Int,
    val isSystemApp: Boolean,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val lastScannedAt: Long,
)
