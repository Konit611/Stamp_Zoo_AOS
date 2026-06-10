package com.konit.stampzooaos.feature.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konit.stampzooaos.core.localization.LanguageStore
import com.konit.stampzooaos.core.qr.QRParser
import com.konit.stampzooaos.core.qr.QRPayload
import com.konit.stampzooaos.data.ZooRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val repo: ZooRepository,
    private val languageStore: LanguageStore
) : ViewModel() {

    fun handleQrResult(
        rawResult: String,
        onStampSuccess: () -> Unit,
        onAlreadyCollected: () -> Unit,
        onAnimalNotFound: () -> Unit,
        onFacilityQr: (String) -> Unit,
        onBingoQr: () -> Unit,
        onEventQr: () -> Unit,
        onInvalidQr: () -> Unit,
        onExpiredSeason: () -> Unit
    ) {
        val parsed = QRParser.parse(rawResult)

        // 시즌 검증: QR에 season이 있고 현재 시즌보다 과거이면 작년(이전 시즌) QR로 안내.
        if (parsed != null && isExpiredSeason(parsed.season)) {
            onExpiredSeason()
            return
        }

        when (parsed) {
            is QRPayload.Data -> {
                when (parsed.type) {
                    QRPayload.Type.ANIMAL -> {
                        val zooData = repo.loadZooData()
                        val animal = zooData.animals.firstOrNull { it.id == parsed.id }
                        if (animal != null) {
                            val facility = zooData.facilities.firstOrNull {
                                it.facilityId == animal.facilityId || it.id == animal.facilityId
                            }
                            val facilityName = facility?.nameJa ?: "Unknown"

                            viewModelScope.launch {
                                val success = repo.collectStamp(
                                    animalId = parsed.id,
                                    qrCode = rawResult,
                                    facilityName = facilityName,
                                    isTestCollection = (parsed.mode == QRPayload.Mode.TEST)
                                )
                                if (success) {
                                    onStampSuccess()
                                } else {
                                    onAlreadyCollected()
                                }
                            }
                        } else {
                            onAnimalNotFound()
                        }
                    }
                    QRPayload.Type.FACILITY -> {
                        onFacilityQr(parsed.id)
                    }
                    QRPayload.Type.BINGO -> {
                        onBingoQr()
                    }
                    QRPayload.Type.EVENT -> {
                        onEventQr()
                    }
                }
            }
            is QRPayload.FacilityAnimal -> {
                val zooData = repo.loadZooData()
                val facility = zooData.facilities.firstOrNull {
                    it.facilityId == parsed.facilityId || it.id == parsed.facilityId
                }
                if (facility == null) {
                    onAnimalNotFound()
                    return
                }
                val facilityAnimals = zooData.animals.filter {
                    it.facilityId == facility.facilityId
                }
                val animal = facilityAnimals.getOrNull(parsed.animalIndex)
                if (animal == null) {
                    onAnimalNotFound()
                    return
                }
                val facilityName = facility.nameJa

                viewModelScope.launch {
                    val success = repo.collectStamp(
                        animalId = animal.id,
                        qrCode = rawResult,
                        facilityName = facilityName,
                        isTestCollection = (parsed.mode == QRPayload.Mode.TEST)
                    )
                    if (success) {
                        onStampSuccess()
                    } else {
                        onAlreadyCollected()
                    }
                }
            }
            else -> {
                onInvalidQr()
            }
        }
    }

    /**
     * QR 시즌이 현재 시즌보다 과거(작년 이전)인지 판정.
     * 둘 다 숫자면 숫자 비교, 아니면 문자열 비교로 폴백. iOS와 동일.
     * season이 없으면(구형 QR) 만료로 보지 않음.
     */
    private fun isExpiredSeason(qrSeason: String?): Boolean {
        if (qrSeason.isNullOrEmpty()) return false
        val current = repo.getCurrentSeason()
        val q = qrSeason.toIntOrNull()
        val c = current.toIntOrNull()
        return if (q != null && c != null) q < c else qrSeason < current
    }
}
