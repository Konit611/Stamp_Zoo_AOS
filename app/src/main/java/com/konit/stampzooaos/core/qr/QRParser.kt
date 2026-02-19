package com.konit.stampzooaos.core.qr

object QRParser {
    fun parse(raw: String): QRPayload? {
        if (!raw.startsWith("stamp_zoo://")) return null
        val segments = raw.removePrefix("stamp_zoo://").split("/")

        val (mode, typeStr, id) = when (segments.size) {
            3 -> Triple(
                if (segments[0] == "test") QRPayload.Mode.TEST else QRPayload.Mode.REAL,
                segments[1],
                segments[2]
            )
            2 -> Triple(QRPayload.Mode.REAL, segments[0], segments[1])
            else -> return null
        }

        val type = when (typeStr) {
            "animal" -> QRPayload.Type.ANIMAL
            "facility" -> QRPayload.Type.FACILITY
            "bingo" -> QRPayload.Type.BINGO
            "event" -> QRPayload.Type.EVENT
            else -> return null
        }

        if (id.isBlank()) return null
        return QRPayload.Data(mode, type, id)
    }
}
