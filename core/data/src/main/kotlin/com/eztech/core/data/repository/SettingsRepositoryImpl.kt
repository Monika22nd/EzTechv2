package com.eztech.core.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eztech.core.domain.model.AppSettings
import com.eztech.core.domain.model.ThemePreference
import com.eztech.core.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.ezTechSettingsDataStore by preferencesDataStore(name = "eztech_settings")

@Singleton
internal class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {
    override val settings: Flow<AppSettings> = context.ezTechSettingsDataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { preferences ->
            AppSettings(
                themePreference = preferences[THEME_PREFERENCE]
                    ?.let { value -> runCatching { ThemePreference.valueOf(value) }.getOrNull() }
                    ?: ThemePreference.SYSTEM,
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
            )
        }

    override suspend fun setThemePreference(themePreference: ThemePreference) {
        context.ezTechSettingsDataStore.edit { preferences ->
            preferences[THEME_PREFERENCE] = themePreference.name
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.ezTechSettingsDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    private companion object {
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }
}
