package com.eztech.feature.home.presentation

import com.eztech.core.domain.model.DashboardSummary

data class HomeUiState(
    val summary: DashboardSummary? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
