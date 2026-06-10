package com.konit.stampzooaos.core.config

import androidx.core.net.toUri
import android.net.Uri

/**
 * 외부 링크(구글폼 등) 모음. iOS AppLinks와 동일.
 * URL이 확정되기 전까지는 빈 문자열로 둔다. 빈 값이면 화면에서 "준비 중" 처리.
 */
object AppLinks {
    /** 설문(アンケート) 구글폼 URL — 미정 */
    const val SURVEY_FORM = ""

    /** 경품 응모 폼 URL — 미정 */
    const val PRIZE_APPLICATION = ""

    /** 문자열을 유효한 Uri로 변환. 비어있으면 null. */
    fun url(string: String): Uri? {
        val trimmed = string.trim()
        if (trimmed.isEmpty()) return null
        return trimmed.toUri()
    }
}
