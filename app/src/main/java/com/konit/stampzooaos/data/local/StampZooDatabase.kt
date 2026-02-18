package com.konit.stampzooaos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.konit.stampzooaos.data.local.dao.BingoAnimalDao
import com.konit.stampzooaos.data.local.dao.StampCollectionDao
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
    version = 1,
    exportSchema = false
)
abstract class StampZooDatabase : RoomDatabase() {
    
    abstract fun bingoAnimalDao(): BingoAnimalDao
    abstract fun stampCollectionDao(): StampCollectionDao
    
    companion object {
        @Volatile
        private var INSTANCE: StampZooDatabase? = null
        
        fun getInstance(context: Context): StampZooDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StampZooDatabase::class.java,
                    "stamp_zoo_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

