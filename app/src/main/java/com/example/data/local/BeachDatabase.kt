package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteBeachEntity::class], version = 1, exportSchema = false)
abstract class BeachDatabase : RoomDatabase() {
    abstract fun beachDao(): BeachDao

    companion object {
        @Volatile
        private var INSTANCE: BeachDatabase? = null

        fun getDatabase(context: Context): BeachDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BeachDatabase::class.java,
                    "playas_almeria.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
