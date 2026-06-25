package com.eztech.feature.home.presentation

import com.eztech.core.domain.model.DashboardSummary
import com.eztech.core.domain.model.Recommendation

data class HomeUiState(
    val summary: DashboardSummary? = null,
    val recommendations: List<Recommendation> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingRecommendations: Boolean = false,
    val errorMessage: String? = null,
    val recommendationErrorMessage: String? = null,
)
