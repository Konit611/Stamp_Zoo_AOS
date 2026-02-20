package com.konit.stampzooaos.core.qr

object QRParser {
    fun parse(raw: String): QRPayload? {
        if (!raw.startsWith("stamp_zoo://")) return null
        val segments = raw.removePrefix("stamp_zoo://").split("/")

        // facility/animal format: stamp_zoo://facility/{facilityId}/animal/{animalIndex}
        // test facility/animal: stamp_zoo://test/facility/{facilityId}/animal/{animalIndex}
        parseFacilityAnimal(segments)?.let { return it }

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

    private fun parseFacilityAnimal(segments: List<String>): QRPayload.FacilityAnimal? {
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
                animalIndex = animalIndex
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
                animalIndex = animalIndex
            )
        }
        return null
    }
}
