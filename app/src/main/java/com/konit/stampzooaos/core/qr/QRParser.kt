package com.konit.stampzooaos.core.qr

object QRParser {
    fun parse(raw: String): QRPayload? {
        if (!raw.startsWith("stamp_zoo://")) return null

        // 쿼리(?season=...) 분리 — 경로 추출 전에 제거해야 구형/신형 QR 모두 호환.
        val season = extractSeason(raw)
        val clean = stripQuery(raw)

        val segments = clean.removePrefix("stamp_zoo://").split("/")

        // facility/animal format: stamp_zoo://facility/{facilityId}/animal/{animalIndex}
        // test facility/animal: stamp_zoo://test/facility/{facilityId}/animal/{animalIndex}
        parseFacilityAnimal(segments, season)?.let { return it }

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
        return QRPayload.Data(mode, type, id, season)
    }

    /** QR 코드에서 season 쿼리 파라미터 추출 (stamp_zoo://...?season=2026). 없으면 null. */
    private fun extractSeason(raw: String): String? {
        val queryIndex = raw.indexOf('?')
        if (queryIndex < 0) return null
        val query = raw.substring(queryIndex + 1)
        for (pair in query.split("&")) {
            val kv = pair.split("=", limit = 2)
            if (kv.size == 2 && kv[0] == "season") {
                val value = kv[1].trim()
                return value.ifEmpty { null }
            }
        }
        return null
    }

    /** 쿼리 문자열(?...)을 제거해 순수 경로만 남긴다. */
    private fun stripQuery(raw: String): String {
        val queryIndex = raw.indexOf('?')
        return if (queryIndex >= 0) raw.substring(0, queryIndex) else raw
    }

    private fun parseFacilityAnimal(segments: List<String>, season: String?): QRPayload.FacilityAnimal? {
        // 4 segments: facility/{facilityId}/animal/{animalIndex} (REAL)
        if (segments.size == 4 &&
            segments[0] == "facility" &&
            segments[2] == "animal"
        ) {
            val facilityId = segments[1]
            val animalIndex = segments[3].toIntOrNull() ?: return null
            if (facilityId.isBlank()) return null
            return QRPayload.FacilityAnimal(
                mode = QRPayload.Mode.REAL,
                facilityId = facilityId,
                animalIndex = animalIndex,
                season = season
            )
        }
        // 5 segments: test/facility/{facilityId}/animal/{animalIndex} (TEST)
        if (segments.size == 5 &&
            segments[0] == "test" &&
            segments[1] == "facility" &&
            segments[3] == "animal"
        ) {
            val facilityId = segments[2]
            val animalIndex = segments[4].toIntOrNull() ?: return null
            if (facilityId.isBlank()) return null
            return QRPayload.FacilityAnimal(
                mode = QRPayload.Mode.TEST,
                facilityId = facilityId,
                animalIndex = animalIndex,
                season = season
            )
        }
        return null
    }
}
