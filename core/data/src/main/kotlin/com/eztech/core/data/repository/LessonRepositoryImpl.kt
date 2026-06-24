package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.data.source.local.LocalLessonDataSource
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonCategory
import com.eztech.core.domain.model.ProgrammingLanguage
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal class LessonRepositoryImpl(
    private val localDataSource: LocalLessonDataSource,
    private val authRepository: AuthRepository,
) : LessonRepository {

    override fun observeLanguages(): Flow<Resource<List<ProgrammingLanguage>>> =
        resourceFlow(localDataSource::getLanguages)

    override fun observeCategories(
        languageId: String,
    ): Flow<Resource<List<LessonCategory>>> = resourceFlow {
        localDataSource.getCategories(languageId)
    }

    override fun observeLessonsByCategory(
        languageId: String,
        categoryId: String,
    ): Flow<Resource<List<Lesson>>> = combine(
        authRepository.observeCurrentUser(),
        localDataSource.progressVersion,
    ) { user, progressVersion ->
        user?.uid to progressVersion
    }.map { (userId, _) ->
        Resource.Success(
            localDataSource.getLessons(
                languageId = languageId,
                categoryId = categoryId,
                userId = userId,
            ),
        ) as Resource<List<Lesson>>
    }.onStart {
        emit(Resource.Loading)
    }.catch { error ->
        emit(error.toResourceError())
    }

    override fun observeLesson(lessonId: String): Flow<Resource<Lesson>> = combine(
        authRepository.observeCurrentUser(),
        localDataSource.progressVersion,
    ) { user, progressVersion ->
        user?.uid to progressVersion
    }.map { (userId, _) ->
        Resource.Success(
            localDataSource.getLesson(
                lessonId = lessonId,
                userId = userId,
            ),
        ) as Resource<Lesson>
    }.onStart {
        emit(Resource.Loading)
    }.catch { error ->
        emit(error.toResourceError())
    }

    override suspend fun markAsWatched(
        userId: String,
        lessonId: String,
    ): Resource<Unit> = runCatching {
        localDataSource.markAsWatched(
            userId = userId,
            lessonId = lessonId,
        )
    }.fold(
        onSuccess = { Resource.Success(Unit) },
        onFailure = { error -> error.toResourceError() },
    )

    private fun <T> resourceFlow(loader: suspend () -> T): Flow<Resource<T>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(loader()))
    }.catch { error ->
        emit(error.toResourceError())
    }

    private fun Throwable.toResourceError() = Resource.Error(
        message = localizedMessage ?: "Unable to load lesson data.",
        cause = this,
    )
}
