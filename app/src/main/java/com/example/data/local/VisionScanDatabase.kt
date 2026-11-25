package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserProfileEntity::class, ScreeningSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VisionScanDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun screeningSessionDao(): ScreeningSessionDao

    companion object {
        @Volatile
        private var INSTANCE: VisionScanDatabase? = null

        fun getInstance(context: Context): VisionScanDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VisionScanDatabase::class.java,
                    "visionscan_ai_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
