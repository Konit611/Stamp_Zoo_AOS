package com.konit.stampzooaos.core.localization

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Application.langDataStore by androidx.datastore.preferences.preferencesDataStore(name = "settings")

class LanguageStore(private val app: Application) {
    private val KEY_LANGUAGE = stringPreferencesKey("language_tag")
    
    private val prefs = app.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    val languageFlow: Flow<String> = app.langDataStore.data.map { preferences ->
        val dataStoreValue = preferences[KEY_LANGUAGE]
        val sharedPrefsValue = prefs.getString("language_tag", null)
        
        // DataStore 값이 있으면 그것을 사용, 없으면 SharedPreferences 값, 둘 다 없으면 "ja"
        dataStoreValue ?: sharedPrefsValue ?: "ja"
    }

    suspend fun setLanguage(tag: String, activity: ComponentActivity?) {
        Log.d("LanguageStore", "Setting language to: $tag")
        
        // SharedPreferences에 먼저 저장 (동기, attachBaseContext용)
        val success = prefs.edit().putString("language_tag", tag).commit()
        Log.d("LanguageStore", "SharedPreferences save success: $success")
        
        // DataStore에 저장 (비동기)
        app.langDataStore.edit { it[KEY_LANGUAGE] = tag }
        Log.d("LanguageStore", "DataStore save completed")
        
        // Activity를 재생성하여 언어 변경 적용 (현재 화면 유지)
        if (activity != null) {
            Log.d("LanguageStore", "Recreating activity")
            activity.recreate()
        } else {
            Log.d("LanguageStore", "Activity is null, cannot recreate")
        }
    }
    
    // 초기화 시 DataStore와 SharedPreferences 동기화
    suspend fun syncDataStores() {
        val sharedPrefsValue = prefs.getString("language_tag", null)
        if (sharedPrefsValue != null) {
            app.langDataStore.edit { preferences ->
                if (preferences[KEY_LANGUAGE] == null) {
                    preferences[KEY_LANGUAGE] = sharedPrefsValue
                }
            }
        }
    }
}

