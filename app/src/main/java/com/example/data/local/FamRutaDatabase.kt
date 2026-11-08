package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.RouteDao
import com.example.data.local.entity.RouteEntity

@Database(entities = [RouteEntity::class], version = 1, exportSchema = false)
abstract class FamRutaDatabase : RoomDatabase() {

    abstract fun routeDao(): RouteDao

    companion object {
        @Volatile
        private var INSTANCE: FamRutaDatabase? = null

        fun getInstance(context: Context): FamRutaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FamRutaDatabase::class.java,
                    "famruta_routes.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
