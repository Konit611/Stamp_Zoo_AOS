package com.konit.stampzooaos.core.qr

sealed interface QRPayload {
    val mode: Mode

    /** QR에 포함된 시즌(연도). 없으면 null. iOS와 동일하게 String. */
    val season: String?

    enum class Mode { TEST, REAL }
    enum class Type { FACILITY, ANIMAL, BINGO, EVENT }

    data class Data(
        override val mode: Mode,
        val type: Type,
        val id: String,
        override val season: String? = null
    ) : QRPayload

    data class FacilityAnimal(
        override val mode: Mode,
        val facilityId: String,
        val animalIndex: Int,
        override val season: String? = null
    ) : QRPayload
}
