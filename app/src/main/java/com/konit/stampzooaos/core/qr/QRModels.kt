package com.konit.stampzooaos.core.qr

sealed interface QRPayload {
    val mode: Mode
    val type: Type
    val id: String

    enum class Mode { TEST, REAL }
    enum class Type { FACILITY, ANIMAL, BINGO, EVENT }

    data class Data(
        override val mode: Mode,
        override val type: Type,
        override val id: String
    ) : QRPayload
}

