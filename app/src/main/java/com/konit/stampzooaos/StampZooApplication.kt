package com.konit.stampzooaos

import android.app.Application
import com.konit.stampzooaos.data.ZooRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class StampZooApplication : Application() {

    @Inject
    lateinit var zooRepository: ZooRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // 시즌이 바뀌었으면 빙고판 초기화 (수집 기록은 보존). iOS의 loadDataIfNeeded 대응.
        appScope.launch {
            zooRepository.syncSeasonIfNeeded()
        }
    }
}
