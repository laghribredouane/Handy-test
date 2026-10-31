package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.DriveTestDao
import com.example.data.entity.DriveTestSample
import com.example.data.entity.DriveTestSession

@Database(
    entities = [DriveTestSession::class, DriveTestSample::class],
    version = 1,
    exportSchema = false
)
abstract class DriveTestDatabase : RoomDatabase() {
    abstract fun driveTestDao(): DriveTestDao

    companion object {
        @Volatile
        private var INSTANCE: DriveTestDatabase? = null

        fun getDatabase(context: Context): DriveTestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DriveTestDatabase::class.java,
                    "netfield_handy_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
