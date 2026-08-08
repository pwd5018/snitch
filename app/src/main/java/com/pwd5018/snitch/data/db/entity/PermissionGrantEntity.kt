package com.pwd5018.snitch.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * [protectionLevel] is a plain String (not a Kotlin enum) so this entity needs no Room
 * TypeConverter registration. Known values live in [ProtectionLevel].
 */
object ProtectionLevel {
    const val NORMAL = "normal"
    const val DANGEROUS = "dangerous"
    const val SIGNATURE = "signature"
    const val SPECIAL = "special"
    const val UNKNOWN = "unknown"
}

@Entity(
    tableName = "permission_grants",
    foreignKeys = [
        ForeignKey(
            entity = InstalledAppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("packageName")],
)
data class PermissionGrantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val permissionName: String,
    val protectionLevel: String,
    val isGranted: Boolean,
)
