package com.pwd5018.snitch.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.pwd5018.snitch.data.db.entity.InstalledAppEntity
import com.pwd5018.snitch.data.db.entity.PermissionGrantEntity
import com.pwd5018.snitch.data.db.entity.RiskFlagEntity

data class AppWithGrantsAndFlags(
    @Embedded val app: InstalledAppEntity,
    @Relation(parentColumn = "packageName", entityColumn = "packageName")
    val grants: List<PermissionGrantEntity>,
    @Relation(parentColumn = "packageName", entityColumn = "packageName")
    val flags: List<RiskFlagEntity>,
)
