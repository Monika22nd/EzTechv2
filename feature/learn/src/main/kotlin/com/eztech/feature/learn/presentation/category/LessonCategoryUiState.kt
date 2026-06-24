package com.eztech.feature.learn.presentation.category

import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonCategory

enum class LearnTab {
    Videos,
    Tutorials,
}

data class LessonCategoryUiState(
    val isLoading: Boolean = true,
    val selectedTab: LearnTab = LearnTab.Videos,
    val categories: List<LessonCategory> = emptyList(),
    val videoLessons: List<Lesson> = emptyList(),
    val errorMessage: String? = null,
)
