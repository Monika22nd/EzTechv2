package com.eztech.core.domain.usecase

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.LessonCategory
import com.eztech.core.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow

class GetLessonCategoriesUseCase(
    private val lessonRepository: LessonRepository,
) {
    operator fun invoke(languageId: String): Flow<Resource<List<LessonCategory>>> =
        lessonRepository.observeCategories(languageId)
}
