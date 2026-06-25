package com.eztech.feature.home.presentation.recommendation

import com.eztech.core.domain.model.Recommendation
import com.eztech.core.domain.model.RecommendationStats

/** UI state for the full Recommendations page. */
data class RecommendationsUiState(
    val recommendations: List<Recommendation> = emptyList(),
    val stats: RecommendationStats? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
