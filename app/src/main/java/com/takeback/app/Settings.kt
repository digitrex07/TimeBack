package com.takeback.app

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore(name = "settings")

class Settings(private val context: Context) {
    private val instagramKey = booleanPreferencesKey("instagram")
    private val youtubeKey = booleanPreferencesKey("youtube")
    val enabled = context.store.data.map { prefs ->
        Enabled(prefs[instagramKey] ?: true, prefs[youtubeKey] ?: true)
    }
    suspend fun setInstagram(value: Boolean) { context.store.edit { it[instagramKey] = value } }
    suspend fun setYouTube(value: Boolean) { context.store.edit { it[youtubeKey] = value } }
}

data class Enabled(val instagram: Boolean, val youtube: Boolean)
