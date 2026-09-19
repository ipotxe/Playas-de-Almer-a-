package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteBeachEntity::class, BeachEntity::class],
    version = 2,
    exportSchema = false
)
abstract class BeachDatabase : RoomDatabase() {
    abstract fun beachDao(): BeachDao
    abstract fun beachInfoDao(): BeachInfoDao

    companion object {
        @Volatile
        private var INSTANCE: BeachDatabase? = null

        fun getDatabase(context: Context): BeachDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BeachDatabase::class.java,
                    "playas_almeria.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
