package com.konit.stampzooaos.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.konit.stampzooaos.data.Animal
import com.konit.stampzooaos.data.Facility

val LocalLanguage = staticCompositionLocalOf { "ja" }

/**
 * Get current language from CompositionLocal
 */
@Composable
fun getCurrentLanguage(): String = LocalLanguage.current

/**
 * Get localized name for Animal based on current language
 */
fun Animal.getLocalizedName(language: String): String {
    return when (language) {
        "ko" -> nameKo
        "en" -> nameEn
        "ja" -> nameJa
        "zh" -> nameZh
        else -> nameJa
    }
}

/**
 * Get localized detail for Animal based on current language
 */
fun Animal.getLocalizedDetail(language: String): String {
    return when (language) {
        "ko" -> detailKo
        "en" -> detailEn
        "ja" -> detailJa
        "zh" -> detailZh
        else -> detailJa
    }
}

/**
 * Get localized name for Facility based on current language
 */
fun Facility.getLocalizedName(language: String): String {
    return when (language) {
        "ko" -> nameKo
        "en" -> nameEn
        "ja" -> nameJa
        "zh" -> nameZh
        else -> nameJa
    }
}

/**
 * Get localized location for Facility based on current language
 */
fun Facility.getLocalizedLocation(language: String): String {
    return when (language) {
        "ko" -> locationKo
        "en" -> locationEn
        "ja" -> locationJa
        "zh" -> locationZh
        else -> locationJa
    }
}

/**
 * Get localized detail for Facility based on current language
 */
fun Facility.getLocalizedDetail(language: String): String {
    return when (language) {
        "ko" -> detailKo
        "en" -> detailEn
        "ja" -> detailJa
        "zh" -> detailZh
        else -> detailJa
    }
}
