package com.eztech.feature.learn.presentation.list

import com.eztech.core.domain.model.Lesson

data class LessonListUiState(
    val categoryName: String = "Lessons",
    val isLoading: Boolean = true,
    val lessons: List<Lesson> = emptyList(),
    val errorMessage: String? = null,
)
