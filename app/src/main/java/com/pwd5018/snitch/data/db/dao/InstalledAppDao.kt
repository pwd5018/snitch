package com.pwd5018.snitch.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.pwd5018.snitch.data.db.entity.InstalledAppEntity
import com.pwd5018.snitch.data.db.relation.AppWithGrantsAndFlags
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalledAppDao {
    // @Upsert (UPDATE-or-INSERT), not @Insert(REPLACE) (DELETE-then-INSERT) — REPLACE would
    // cascade-delete this row's permission_grants/risk_flags children on every rescan.
    @Upsert
    suspend fun upsert(app: InstalledAppEntity)

    @Transaction
    @Query("SELECT * FROM installed_apps ORDER BY appLabel")
    fun observeAllWithDetails(): Flow<List<AppWithGrantsAndFlags>>

    /** Drops rows for apps no longer installed, keeping a rescan idempotent. */
    @Query("DELETE FROM installed_apps WHERE packageName NOT IN (:currentPackageNames)")
    suspend fun deleteMissing(currentPackageNames: List<String>)
}
