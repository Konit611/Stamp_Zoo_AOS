package com.konit.stampzooaos

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.konit.stampzooaos.core.localization.LanguageStore
import com.konit.stampzooaos.ui.theme.StampZooAosTheme
import com.konit.stampzooaos.ui.navigation.RootNavHost
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        // SharedPreferences에서 저장된 언어 설정을 동기적으로 읽기
        val prefs = newBase.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val language = prefs.getString("language_tag", "ja") ?: "ja"
        Log.d("MainActivity", "attachBaseContext: language = $language")
        val context = updateLocale(newBase, language)
        super.attachBaseContext(context)
    }
    
    private fun updateLocale(context: Context, languageTag: String): Context {
        val locale = if (languageTag.isEmpty()) {
            Locale.forLanguageTag("ja")
        } else {
            Locale.forLanguageTag(languageTag)
        }
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // DataStore와 SharedPreferences 동기화
        lifecycleScope.launch {
            val langStore = LanguageStore(application)
            langStore.syncDataStores()
        }
        
        enableEdgeToEdge()
        setContent {
            StampZooAosTheme {
                RootNavHost()
            }
        }
    }
}