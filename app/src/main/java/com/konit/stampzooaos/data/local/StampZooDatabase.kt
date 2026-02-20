package com.konit.stampzooaos.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.konit.stampzooaos.data.local.dao.BingoAnimalDao
import com.konit.stampzooaos.data.local.dao.StampCollectionDao
import com.konit.stampzooaos.BuildConfig
import com.konit.stampzooaos.data.local.entity.BingoAnimalEntity
import com.konit.stampzooaos.data.local.entity.StampCollectionEntity

/**
 * StampZoo 앱의 메인 Room Database
 */
@Database(
    entities = [
        BingoAnimalEntity::class,
        StampCollectionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StampZooDatabase : RoomDatabase() {

    abstract fun bingoAnimalDao(): BingoAnimalDao
    abstract fun stampCollectionDao(): StampCollectionDao

    companion object {
        @Volatile
        private var INSTANCE: StampZooDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stamp_collections ADD COLUMN animal_id TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): StampZooDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = try {
                    Room.databaseBuilder(
                        context.applicationContext,
                        StampZooDatabase::class.java,
                        "stamp_zoo_database"
                    )
                        .addMigrations(MIGRATION_1_2)
                        .fallbackToDestructiveMigration()
                        .build()
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.e("StampZooDatabase", "DB creation failed, falling back to in-memory", e)
                    }
                    Room.inMemoryDatabaseBuilder(
                        context.applicationContext,
                        StampZooDatabase::class.java
                    ).build()
                }
                INSTANCE = instance
                instance
            }
        }
    }
}
