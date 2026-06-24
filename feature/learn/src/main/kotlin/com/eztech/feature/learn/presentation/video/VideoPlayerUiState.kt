package com.eztech.feature.learn.presentation.video

import com.eztech.core.domain.model.Lesson

data class VideoPlayerUiState(
    val isLoading: Boolean = true,
    val lesson: Lesson? = null,
    val errorMessage: String? = null,
    val message: String? = null,
)
