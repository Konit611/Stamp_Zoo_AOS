package com.konit.stampzooaos.core.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.konit.stampzooaos.data.Animal
import com.konit.stampzooaos.data.Facility

/**
 * Get current language from LanguageStore
 */
@Composable
fun getCurrentLanguage(): String {
    val context = LocalContext.current
    val languageStore = LanguageStore(context.applicationContext as android.app.Application)
    val language by languageStore.languageFlow.collectAsState(initial = "ja")
    return language
}

/**
 * Get localized name for Animal based on current language
 */
fun Animal.getLocalizedName(language: String): String {
    return when (language) {
        "ko" -> nameKo
        "en" -> nameEn
        "ja" -> nameJa
        "zh" -> nameZh
        else -> nameJa // fallback to Japanese
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
        else -> detailJa // fallback to Japanese
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
        else -> nameJa // fallback to Japanese
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
        else -> locationJa // fallback to Japanese
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
        else -> detailJa // fallback to Japanese
    }
}



