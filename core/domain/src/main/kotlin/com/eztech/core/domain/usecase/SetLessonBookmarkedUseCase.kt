package com.eztech.core.domain.usecase

import com.eztech.core.common.Resource
import com.eztech.core.domain.repository.LessonRepository

class SetLessonBookmarkedUseCase(
    private val lessonRepository: LessonRepository,
) {
    suspend operator fun invoke(
        userId: String,
        lessonId: String,
        bookmarked: Boolean,
    ): Resource<Unit> {
        if (lessonId.isBlank()) return Resource.Error("Lesson ID is required.")
        return lessonRepository.setBookmarked(
            userId = userId,
            lessonId = lessonId,
            bookmarked = bookmarked,
        )
    }
}
