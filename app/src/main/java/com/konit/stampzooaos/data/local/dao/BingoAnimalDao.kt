package com.konit.stampzooaos.data.local.dao

import androidx.room.*
import com.konit.stampzooaos.data.local.entity.BingoAnimalEntity
import kotlinx.coroutines.flow.Flow

/**
 * 빙고 동물 데이터에 접근하는 DAO
 */
@Dao
interface BingoAnimalDao {
    
    @Query("SELECT * FROM bingo_animals ORDER BY bingo_number ASC")
    fun getAllBingoAnimals(): Flow<List<BingoAnimalEntity>>
    
    @Query("SELECT * FROM bingo_animals WHERE bingo_number = :bingoNumber LIMIT 1")
    suspend fun getBingoAnimalByNumber(bingoNumber: Int): BingoAnimalEntity?
    
    @Query("SELECT * FROM bingo_animals WHERE animal_id = :animalId LIMIT 1")
    suspend fun getBingoAnimalByAnimalId(animalId: String): BingoAnimalEntity?
    
    @Query("SELECT COUNT(*) FROM bingo_animals")
    fun getCollectedCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM bingo_animals")
    suspend fun getCollectedCountSync(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBingoAnimal(bingoAnimal: BingoAnimalEntity): Long
    
    @Delete
    suspend fun deleteBingoAnimal(bingoAnimal: BingoAnimalEntity)
    
    @Query("DELETE FROM bingo_animals")
    suspend fun deleteAllBingoAnimals()
    
    @Query("SELECT * FROM bingo_animals WHERE id = :id LIMIT 1")
    suspend fun getBingoAnimalById(id: Long): BingoAnimalEntity?
}

