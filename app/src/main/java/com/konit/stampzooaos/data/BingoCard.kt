package com.konit.stampzooaos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BingoCard(
    val id: String,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("name_en") val nameEn: String,
    @SerialName("name_ja") val nameJa: String,
    @SerialName("name_zh") val nameZh: String,
    @SerialName("description_ko") val descriptionKo: String,
    @SerialName("description_en") val descriptionEn: String,
    @SerialName("description_ja") val descriptionJa: String,
    @SerialName("description_zh") val descriptionZh: String,
    @SerialName("grid_size") val gridSize: Int,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("display_order") val displayOrder: Int
)