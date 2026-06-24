package com.eztech.core.domain.usecase

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.ProgrammingLanguage
import com.eztech.core.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow

class GetProgrammingLanguagesUseCase(
    private val lessonRepository: LessonRepository,
) {
    operator fun invoke(): Flow<Resource<List<ProgrammingLanguage>>> =
        lessonRepository.observeLanguages()
}
