package com.eztech.feature.learn.presentation.tutorial

import com.eztech.core.domain.model.Lesson

data class TutorialArticleUiState(
    val isLoading: Boolean = true,
    val lesson: Lesson? = null,
    val errorMessage: String? = null,
    val message: String? = null,
)
