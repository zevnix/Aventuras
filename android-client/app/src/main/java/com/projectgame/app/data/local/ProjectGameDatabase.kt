package com.projectgame.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.projectgame.app.data.local.dao.PlayerDao
import com.projectgame.app.data.local.entity.GameConfigEntity
import com.projectgame.app.data.local.entity.PetEvolutionEntity
import com.projectgame.app.data.local.entity.PlayerProfileEntity

@Database(
    entities = [
        PlayerProfileEntity::class,
        PetEvolutionEntity::class,
        GameConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ProjectGameDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao

    companion object {
        @Volatile
        private var INSTANCE: ProjectGameDatabase? = null

        fun getDatabase(context: Context): ProjectGameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProjectGameDatabase::class.java,
                    "projectgame_database"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
