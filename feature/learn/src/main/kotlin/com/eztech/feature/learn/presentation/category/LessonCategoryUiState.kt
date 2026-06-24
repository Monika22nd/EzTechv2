package com.eztech.feature.learn.presentation.category

import com.eztech.core.domain.model.LessonCategory

data class LessonCategoryUiState(
    val isLoading: Boolean = true,
    val categories: List<LessonCategory> = emptyList(),
    val errorMessage: String? = null,
)
