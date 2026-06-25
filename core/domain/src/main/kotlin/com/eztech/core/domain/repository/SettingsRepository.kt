package com.eztech.core.domain.repository

import com.eztech.core.domain.model.AppSettings
import com.eztech.core.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setThemePreference(themePreference: ThemePreference)

    suspend fun setNotificationsEnabled(enabled: Boolean)
}
