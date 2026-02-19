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
        onInvalidQr: () -> Unit
    ) {
        val parsed = QRParser.parse(rawResult)
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
            else -> {
                onInvalidQr()
            }
        }
    }
}
