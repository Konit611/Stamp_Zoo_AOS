package com.konit.stampzooaos.data

import android.content.Context
import com.konit.stampzooaos.data.local.StampZooDatabase
import com.konit.stampzooaos.data.local.dao.BingoAnimalDao
import com.konit.stampzooaos.data.local.dao.StampCollectionDao
import com.konit.stampzooaos.data.local.entity.BingoAnimalEntity
import com.konit.stampzooaos.data.local.entity.StampCollectionEntity
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class ZooRepository(private val context: Context) {
    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
        }
    }

    // Room Database DAOs
    private val database: StampZooDatabase by lazy {
        StampZooDatabase.getInstance(context)
    }

    private val bingoAnimalDao: BingoAnimalDao by lazy {
        database.bingoAnimalDao()
    }

    private val stampCollectionDao: StampCollectionDao by lazy {
        database.stampCollectionDao()
    }

    // JSON 캐싱
    @Volatile
    private var cachedZooData: ZooData? = null

    fun loadZooData(): ZooData {
        cachedZooData?.let { return it }
        val assetManager = context.assets
        return assetManager.open("zoo_data_2025_09_01.json").use { input ->
            val text = input.bufferedReader().readText()
            json.decodeFromString(ZooData.serializer(), text)
        }.also { cachedZooData = it }
    }

    // === BingoAnimal 관련 메서드 ===

    fun getAllBingoAnimals(): Flow<List<BingoAnimalEntity>> {
        return bingoAnimalDao.getAllBingoAnimals()
    }

    suspend fun getBingoAnimalByAnimalId(animalId: String): BingoAnimalEntity? {
        return bingoAnimalDao.getBingoAnimalByAnimalId(animalId)
    }

    fun getCollectedCount(): Flow<Int> {
        return bingoAnimalDao.getCollectedCount()
    }

    suspend fun getCollectedCountSync(): Int {
        return bingoAnimalDao.getCollectedCountSync()
    }

    suspend fun insertBingoAnimal(bingoAnimal: BingoAnimalEntity): Long {
        return bingoAnimalDao.insertBingoAnimal(bingoAnimal)
    }

    suspend fun deleteAllBingoAnimals() {
        bingoAnimalDao.deleteAllBingoAnimals()
    }

    // === StampCollection 관련 메서드 ===

    fun getAllStampCollections(): Flow<List<StampCollectionEntity>> {
        return stampCollectionDao.getAllStampCollections()
    }

    suspend fun insertStampCollection(stampCollection: StampCollectionEntity): Long {
        return stampCollectionDao.insertStampCollection(stampCollection)
    }

    suspend fun deleteAllStampCollections() {
        stampCollectionDao.deleteAllStampCollections()
    }

    /**
     * 새로운 스탬프를 수집하는 통합 메서드
     * BingoAnimal과 StampCollection을 모두 트랜잭션으로 저장
     */
    suspend fun collectStamp(
        animalId: String,
        qrCode: String,
        facilityName: String,
        userLatitude: Double? = null,
        userLongitude: Double? = null,
        isTestCollection: Boolean = false
    ): Boolean {
        return database.withTransaction {
            // 이미 수집된 동물인지 확인
            val existing = bingoAnimalDao.getBingoAnimalByAnimalId(animalId)
            if (existing != null) {
                return@withTransaction false
            }

            // 다음 빙고 번호 계산 (1-9)
            val currentCount = bingoAnimalDao.getCollectedCountSync()
            if (currentCount >= 9) {
                return@withTransaction false
            }
            val nextBingoNumber = currentCount + 1

            val now = System.currentTimeMillis()

            // BingoAnimal 저장
            val bingoAnimal = BingoAnimalEntity(
                bingoNumber = nextBingoNumber,
                animalId = animalId,
                collectedAt = now,
                qrCode = qrCode
            )
            bingoAnimalDao.insertBingoAnimal(bingoAnimal)

            // StampCollection 저장
            val stampCollection = StampCollectionEntity(
                bingoNumber = nextBingoNumber,
                collectedAt = now,
                qrCode = qrCode,
                facilityName = facilityName,
                userLatitude = userLatitude,
                userLongitude = userLongitude,
                isTestCollection = isTestCollection
            )
            stampCollectionDao.insertStampCollection(stampCollection)

            true
        }
    }
}
