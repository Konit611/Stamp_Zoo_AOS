package com.konit.stampzooaos.core.qr

import android.net.Uri

object QRParser {
    fun parse(raw: String): QRPayload? {
        if (!raw.startsWith("stamp_zoo://")) return null
        
        // iOS와 동일한 방식으로 파싱
        val withoutScheme = raw.removePrefix("stamp_zoo://")
        val segments = withoutScheme.split("/")
        
        if (segments.size < 2) return null
        
        val mode: QRPayload.Mode
        val type: QRPayload.Type
        val id: String
        
        when {
            // stamp_zoo://test/animal/uuid
            raw.startsWith("stamp_zoo://test/animal/") -> {
                val uuidString = raw.removePrefix("stamp_zoo://test/animal/")
                if (uuidString.isBlank()) return null
                mode = QRPayload.Mode.TEST
                type = QRPayload.Type.ANIMAL
                id = uuidString
            }
            // stamp_zoo://test/facility/facilityId
            raw.startsWith("stamp_zoo://test/facility/") -> {
                val facilityId = raw.removePrefix("stamp_zoo://test/facility/")
                if (facilityId.isBlank()) return null
                mode = QRPayload.Mode.TEST
                type = QRPayload.Type.FACILITY
                id = facilityId
            }
            // stamp_zoo://test/bingo/id
            raw.startsWith("stamp_zoo://test/bingo/") -> {
                val bingoId = raw.removePrefix("stamp_zoo://test/bingo/")
                if (bingoId.isBlank()) return null
                mode = QRPayload.Mode.TEST
                type = QRPayload.Type.BINGO
                id = bingoId
            }
            // stamp_zoo://test/event/id
            raw.startsWith("stamp_zoo://test/event/") -> {
                val eventId = raw.removePrefix("stamp_zoo://test/event/")
                if (eventId.isBlank()) return null
                mode = QRPayload.Mode.TEST
                type = QRPayload.Type.EVENT
                id = eventId
            }
            // stamp_zoo://animal/uuid
            raw.startsWith("stamp_zoo://animal/") -> {
                val uuidString = raw.removePrefix("stamp_zoo://animal/")
                if (uuidString.isBlank()) return null
                mode = QRPayload.Mode.REAL
                type = QRPayload.Type.ANIMAL
                id = uuidString
            }
            // stamp_zoo://real/facility/facilityId
            raw.startsWith("stamp_zoo://real/facility/") -> {
                val facilityId = raw.removePrefix("stamp_zoo://real/facility/")
                if (facilityId.isBlank()) return null
                mode = QRPayload.Mode.REAL
                type = QRPayload.Type.FACILITY
                id = facilityId
            }
            // stamp_zoo://real/bingo/id
            raw.startsWith("stamp_zoo://real/bingo/") -> {
                val bingoId = raw.removePrefix("stamp_zoo://real/bingo/")
                if (bingoId.isBlank()) return null
                mode = QRPayload.Mode.REAL
                type = QRPayload.Type.BINGO
                id = bingoId
            }
            // stamp_zoo://real/event/id
            raw.startsWith("stamp_zoo://real/event/") -> {
                val eventId = raw.removePrefix("stamp_zoo://real/event/")
                if (eventId.isBlank()) return null
                mode = QRPayload.Mode.REAL
                type = QRPayload.Type.EVENT
                id = eventId
            }
            else -> return null
        }
        
        return QRPayload.Data(mode, type, id)
    }
}

