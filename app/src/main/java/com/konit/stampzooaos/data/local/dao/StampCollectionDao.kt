package com.konit.stampzooaos.data.local.dao

import androidx.room.*
import com.konit.stampzooaos.data.local.entity.StampCollectionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 스탬프 수집 기록 데이터에 접근하는 DAO
 */
@Dao
interface StampCollectionDao {
    
    @Query("SELECT * FROM stamp_collections ORDER BY collected_at DESC")
    fun getAllStampCollections(): Flow<List<StampCollectionEntity>>
    
    @Query("SELECT * FROM stamp_collections WHERE bingo_number = :bingoNumber LIMIT 1")
    suspend fun getStampCollectionByNumber(bingoNumber: Int): StampCollectionEntity?
    
    @Query("SELECT * FROM stamp_collections WHERE is_test_collection = :isTest ORDER BY collected_at DESC")
    fun getStampCollectionsByTestFlag(isTest: Boolean): Flow<List<StampCollectionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStampCollection(stampCollection: StampCollectionEntity): Long
    
    @Delete
    suspend fun deleteStampCollection(stampCollection: StampCollectionEntity)
    
    @Query("DELETE FROM stamp_collections")
    suspend fun deleteAllStampCollections()
    
    @Query("SELECT * FROM stamp_collections WHERE id = :id LIMIT 1")
    suspend fun getStampCollectionById(id: Long): StampCollectionEntity?
}

