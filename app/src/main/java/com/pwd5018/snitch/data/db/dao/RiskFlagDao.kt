package com.pwd5018.snitch.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pwd5018.snitch.data.db.entity.RiskFlagEntity

@Dao
interface RiskFlagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flags: List<RiskFlagEntity>)

    @Query("DELETE FROM risk_flags WHERE packageName = :packageName")
    suspend fun deleteForPackage(packageName: String)
}
