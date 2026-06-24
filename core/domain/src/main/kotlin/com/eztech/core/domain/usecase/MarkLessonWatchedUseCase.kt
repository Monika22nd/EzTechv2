package com.eztech.core.domain.usecase

import com.eztech.core.common.Resource
import com.eztech.core.domain.repository.LessonRepository

class MarkLessonWatchedUseCase(
    private val lessonRepository: LessonRepository,
) {
    suspend operator fun invoke(
        userId: String,
        lessonId: String,
    ): Resource<Unit> = lessonRepository.markAsWatched(
        userId = userId,
        lessonId = lessonId,
    )
}
