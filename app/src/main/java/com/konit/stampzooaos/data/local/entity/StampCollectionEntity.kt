package com.konit.stampzooaos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 스탬프 수집 기록을 저장하는 Entity
 * iOS의 StampCollection 모델과 동일한 구조
 */
@Entity(tableName = "stamp_collections")
data class StampCollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "bingo_number")
    val bingoNumber: Int,
    
    @ColumnInfo(name = "collected_at")
    val collectedAt: Long, // timestamp in milliseconds
    
    @ColumnInfo(name = "qr_code")
    val qrCode: String,
    
    @ColumnInfo(name = "facility_name")
    val facilityName: String,
    
    @ColumnInfo(name = "user_latitude")
    val userLatitude: Double? = null,
    
    @ColumnInfo(name = "user_longitude")
    val userLongitude: Double? = null,
    
    @ColumnInfo(name = "is_test_collection")
    val isTestCollection: Boolean = false,

    @ColumnInfo(name = "animal_id")
    val animalId: String = ""
)

