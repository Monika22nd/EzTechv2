package com.eztech.core.domain.usecase

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow

class GetLessonUseCase(
    private val lessonRepository: LessonRepository,
) {
    operator fun invoke(lessonId: String): Flow<Resource<Lesson>> =
        lessonRepository.observeLesson(lessonId)
}
