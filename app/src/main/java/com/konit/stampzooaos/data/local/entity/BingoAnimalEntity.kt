package com.konit.stampzooaos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 빙고 게임에서 수집한 동물 정보를 저장하는 Entity
 * iOS의 BingoAnimal 모델과 동일한 구조
 */
@Entity(tableName = "bingo_animals")
data class BingoAnimalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "bingo_number")
    val bingoNumber: Int, // 1-9 (QR 스캔 순서대로 부여)
    
    @ColumnInfo(name = "animal_id")
    val animalId: String, // Animal의 ID
    
    @ColumnInfo(name = "collected_at")
    val collectedAt: Long, // timestamp in milliseconds
    
    @ColumnInfo(name = "qr_code")
    val qrCode: String // 스캔한 QR 코드
)

