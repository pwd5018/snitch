package com.pwd5018.snitch.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pwd5018.snitch.data.db.entity.PermissionGrantEntity

@Dao
interface PermissionGrantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(grants: List<PermissionGrantEntity>)

    @Query("DELETE FROM permission_grants WHERE packageName = :packageName")
    suspend fun deleteForPackage(packageName: String)
}
