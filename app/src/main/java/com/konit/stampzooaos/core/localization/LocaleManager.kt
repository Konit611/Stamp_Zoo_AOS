package com.konit.stampzooaos.core.localization

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleManager {
    fun updateLocale(app: Application, languageTag: String) {
        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)
        val resources = app.resources
        val config = Configuration(resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            config.setLocales(android.os.LocaleList.forLanguageTags(languageTag))
        } else {
            config.setLocale(locale)
        }
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

