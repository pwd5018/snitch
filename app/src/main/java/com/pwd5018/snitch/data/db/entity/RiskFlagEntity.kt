package com.pwd5018.snitch.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Round-1 flag types. Reserved for the later manifest-static-analysis round:
 * DEBUGGABLE, ALLOW_BACKUP, CLEARTEXT_TRAFFIC, EXPORTED_UNGUARDED, OUTDATED_TARGET_SDK. */
object RiskFlagType {
    const val BACKGROUND_LOCATION = "BACKGROUND_LOCATION"
    const val MIC_NO_PURPOSE = "MIC_NO_PURPOSE"
    const val CAMERA_NO_PURPOSE = "CAMERA_NO_PURPOSE"
    const val SMS_READ = "SMS_READ"
    const val CONTACTS_READ = "CONTACTS_READ"
}

object RiskSeverity {
    const val LOW = "LOW"
    const val MEDIUM = "MEDIUM"
    const val HIGH = "HIGH"
}

@Entity(
    tableName = "risk_flags",
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
data class RiskFlagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val flagType: String,
    val severity: String,
    val detail: String,
    val computedAt: Long,
)
