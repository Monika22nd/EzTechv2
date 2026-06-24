package com.eztech.core.domain.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonCategory
import com.eztech.core.domain.model.LessonContentType
import com.eztech.core.domain.model.ProgrammingLanguage
import kotlinx.coroutines.flow.Flow

interface LessonRepository {
    fun observeLanguages(): Flow<Resource<List<ProgrammingLanguage>>>

    fun observeCategories(languageId: String): Flow<Resource<List<LessonCategory>>>

    fun observeLessonsByCategory(
        languageId: String,
        categoryId: String,
    ): Flow<Resource<List<Lesson>>>

    fun observeLessonsByType(
        languageId: String,
        type: LessonContentType,
    ): Flow<Resource<List<Lesson>>>

    fun observeLesson(lessonId: String): Flow<Resource<Lesson>>

    suspend fun markAsWatched(
        userId: String,
        lessonId: String,
    ): Resource<Unit>
}
