package com.eztech.feature.learn.presentation.bookmarks

import com.eztech.core.domain.model.Lesson

data class BookmarkedLessonsUiState(
    val isLoading: Boolean = true,
    val lessons: List<Lesson> = emptyList(),
    val errorMessage: String? = null,
    val message: String? = null,
)
