package com.konit.stampzooaos.core.qr

sealed interface QRPayload {
    val mode: Mode

    enum class Mode { TEST, REAL }
    enum class Type { FACILITY, ANIMAL, BINGO, EVENT }

    data class Data(
        override val mode: Mode,
        val type: Type,
        val id: String
    ) : QRPayload

    data class FacilityAnimal(
        override val mode: Mode,
        val facilityId: String,
        val animalIndex: Int
    ) : QRPayload
}
