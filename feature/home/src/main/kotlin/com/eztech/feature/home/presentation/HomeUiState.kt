package com.eztech.feature.home.presentation

import com.eztech.core.domain.model.DashboardSummary
import com.eztech.core.domain.model.Recommendation
import com.eztech.core.domain.model.RecommendationStats

/**
 * Single state object rendered by HomeScreen.
 *
 * Summary data and recommendation data are kept separate because they load from different use cases
 * and one can succeed while the other is still loading.
 */
data class HomeUiState(
    val summary: DashboardSummary? = null,
    val recommendations: List<Recommendation> = emptyList(),
    val recommendationStats: RecommendationStats? = null,
    val isLoading: Boolean = true,
    val isLoadingRecommendations: Boolean = false,
    val errorMessage: String? = null,
    val recommendationErrorMessage: String? = null,
)
