package com.konit.stampzooaos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ZooData(
    val metadata: Metadata,
    val facilities: List<Facility>,
    val animals: List<Animal>,
    @SerialName("bingoCards") val bingoCards: List<BingoCard>,
    @SerialName("refresh_bingo_animals") val refreshBingoAnimals: Boolean
)

@Serializable
data class Metadata(
    val version: String,
    @SerialName("last_updated") val lastUpdated: String,
    val description: String,
    @SerialName("data_count") val dataCount: Int
)

@Serializable
data class Facility(
    val id: String,
    @SerialName("facility_id") val facilityId: String,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("name_en") val nameEn: String,
    @SerialName("name_ja") val nameJa: String,
    @SerialName("name_zh") val nameZh: String,
    val type: String,
    @SerialName("location_ko") val locationKo: String,
    @SerialName("location_en") val locationEn: String,
    @SerialName("location_ja") val locationJa: String,
    @SerialName("location_zh") val locationZh: String,
    val image: String? = null,
    @SerialName("logo_image") val logoImage: String? = null,
    @SerialName("map_image") val mapImage: String? = null,
    @SerialName("map_link") val mapLink: String? = null,
    // 상세 설명 필드를 여기에 추가해야 이전 오류를 해결할 수 있습니다.
    @SerialName("detail_ko") val detailKo: String,
    @SerialName("detail_en") val detailEn: String,
    @SerialName("detail_ja") val detailJa: String,
    @SerialName("detail_zh") val detailZh: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("validation_radius") val validationRadius: Double? = null
)
