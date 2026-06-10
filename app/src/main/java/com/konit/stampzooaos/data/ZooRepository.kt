package com.konit.stampzooaos.data

import android.content.Context
import android.util.Log
import com.konit.stampzooaos.BuildConfig
import com.konit.stampzooaos.data.local.StampZooDatabase
import com.konit.stampzooaos.data.local.dao.BingoAnimalDao
import com.konit.stampzooaos.data.local.dao.StampCollectionDao
import com.konit.stampzooaos.data.local.entity.BingoAnimalEntity
import com.konit.stampzooaos.data.local.entity.StampCollectionEntity
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class ZooRepository(private val context: Context) {

    companion object {
        // 앱 최초 출시 시즌 (JSON에 season이 없을 때의 폴백). iOS initialSeason과 동일.
        const val INITIAL_SEASON = "2025"
        // 동적 로딩이 실패했을 때의 최종 폴백 파일
        private const val FALLBACK_ASSET = "zoo_data_2025_09_01.json"
        private val DATA_FILE_REGEX = Regex("""zoo_data_\d{4}_\d{2}_\d{2}\.json""")
        private const val SEASON_PREFS = "zoo_data_prefs"
        private const val KEY_LAST_REFRESHED_SEASON = "zoo_data_last_refreshed_season"
        private const val TAG = "ZooRepository"
    }

    private val seasonPrefs by lazy {
        context.getSharedPreferences(SEASON_PREFS, Context.MODE_PRIVATE)
    }

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
        val assetName = latestDataAsset()
        return try {
            assetManager.open(assetName).use { input ->
                val text = input.bufferedReader().readText()
                json.decodeFromString(ZooData.serializer(), text)
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Failed to load $assetName, falling back to $FALLBACK_ASSET", e)
            assetManager.open(FALLBACK_ASSET).use { input ->
                val text = input.bufferedReader().readText()
                json.decodeFromString(ZooData.serializer(), text)
            }
        }.also { cachedZooData = it }
    }

    /**
     * assets에서 zoo_data_YYYY_MM_DD.json 형식 파일을 열거해 날짜가 가장 최신인 파일명을 반환.
     * 파일명이 YYYY_MM_DD라 사전순 = 날짜순. 후보가 없으면 폴백 파일명.
     */
    private fun latestDataAsset(): String {
        return try {
            context.assets.list("")
                ?.filter { DATA_FILE_REGEX.matches(it) }
                ?.maxOrNull()
                ?: FALLBACK_ASSET
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Failed to list assets", e)
            FALLBACK_ASSET
        }
    }

    /** 현재 로드된 시즌. JSON metadata.season이 없으면 INITIAL_SEASON. */
    fun getCurrentSeason(): String {
        return loadZooData().metadata.season ?: INITIAL_SEASON
    }

    /**
     * 시즌이 바뀌었으면 빙고판(bingo_animals)만 초기화한다.
     * 수집 기록(stamp_collections)은 도감용으로 영구 보존 — iOS와 동일.
     * 앱 시작 시 1회 호출.
     */
    suspend fun syncSeasonIfNeeded() {
        val currentSeason = getCurrentSeason()
        val storedSeason = seasonPrefs.getString(KEY_LAST_REFRESHED_SEASON, "") ?: ""

        // 저장된 시즌이 있고(최초 실행 아님) 시즌이 바뀐 경우에만 빙고 리셋
        if (storedSeason.isNotEmpty() && storedSeason != currentSeason) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Season changed: $storedSeason -> $currentSeason, resetting bingo board")
            bingoAnimalDao.deleteAllBingoAnimals()
            if (BuildConfig.DEBUG) Log.d(TAG, "Bingo board cleared. StampCollection preserved.")
        }

        // 리셋 여부와 관계없이 항상 최신 시즌 저장
        seasonPrefs.edit().putString(KEY_LAST_REFRESHED_SEASON, currentSeason).apply()
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

    fun getCollectedAnimalIds(): Flow<List<String>> {
        return stampCollectionDao.getCollectedAnimalIds()
    }

    suspend fun getAllStampCollectionsSync(): List<StampCollectionEntity> {
        return stampCollectionDao.getAllStampCollectionsSync()
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
                isTestCollection = isTestCollection,
                animalId = animalId
            )
            stampCollectionDao.insertStampCollection(stampCollection)

            true
        }
    }
}
