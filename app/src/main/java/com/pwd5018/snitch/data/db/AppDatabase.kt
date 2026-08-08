package com.pwd5018.snitch.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pwd5018.snitch.data.db.dao.InstalledAppDao
import com.pwd5018.snitch.data.db.dao.PermissionGrantDao
import com.pwd5018.snitch.data.db.dao.RiskFlagDao
import com.pwd5018.snitch.data.db.entity.InstalledAppEntity
import com.pwd5018.snitch.data.db.entity.PermissionGrantEntity
import com.pwd5018.snitch.data.db.entity.RiskFlagEntity

@Database(
    entities = [InstalledAppEntity::class, PermissionGrantEntity::class, RiskFlagEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun installedAppDao(): InstalledAppDao
    abstract fun permissionGrantDao(): PermissionGrantDao
    abstract fun riskFlagDao(): RiskFlagDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "snitch.db",
                ).build().also { instance = it }
            }
    }
}
