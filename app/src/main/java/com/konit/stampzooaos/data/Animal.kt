package com.konit.stampzooaos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Animal(
    val id: String,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("name_en") val nameEn: String,
    @SerialName("name_ja") val nameJa: String,
    @SerialName("name_zh") val nameZh: String,
    @SerialName("detail_ko") val detailKo: String,
    @SerialName("detail_en") val detailEn: String,
    @SerialName("detail_ja") val detailJa: String,
    @SerialName("detail_zh") val detailZh: String,
    val image: String? = null,
    @SerialName("stamp_image") val stampImage: String? = null,
    @SerialName("facility_id") val facilityId: String
)