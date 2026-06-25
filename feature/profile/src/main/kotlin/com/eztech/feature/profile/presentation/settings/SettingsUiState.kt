package com.eztech.feature.profile.presentation.settings

import com.eztech.core.domain.model.AppSettings

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
)
