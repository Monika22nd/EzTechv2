package com.eztech.feature.home.presentation.recommendation

import com.eztech.core.domain.model.Recommendation

data class RecommendationsUiState(
    val recommendations: List<Recommendation> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
