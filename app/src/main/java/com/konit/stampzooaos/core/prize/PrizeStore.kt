package com.konit.stampzooaos.core.prize

import android.app.Application
import android.content.Context

/**
 * 경품 응모 완료 상태를 시즌별로 저장/조회. iOS PrizeApplicationStore와 동일.
 * 시즌이 바뀌면 키가 달라지므로 새 시즌에는 다시 응모할 수 있다.
 */
class PrizeStore(app: Application) {

    private val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** 해당 시즌에 응모가 완료되었는지 여부. */
    fun isApplied(season: String): Boolean = prefs.getBoolean(key(season), false)

    /** 해당 시즌의 응모 완료 상태 설정. */
    fun setApplied(applied: Boolean, season: String) {
        prefs.edit().putBoolean(key(season), applied).apply()
    }

    private fun key(season: String) = "prize_applied_$season"

    companion object {
        private const val PREFS_NAME = "prize_prefs"
    }
}
