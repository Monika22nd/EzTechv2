package com.eztech.core.domain.usecase

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonContentType
import com.eztech.core.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow

class GetLessonsByTypeUseCase(
    private val lessonRepository: LessonRepository,
) {
    operator fun invoke(
        languageId: String,
        type: LessonContentType,
    ): Flow<Resource<List<Lesson>>> =
        lessonRepository.observeLessonsByType(languageId, type)
}
