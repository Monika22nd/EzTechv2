package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.data.source.local.LocalLessonDataSource
import com.eztech.core.data.source.remote.FirebaseLessonDataSource
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonCategory
import com.eztech.core.domain.model.LessonContentType
import com.eztech.core.domain.model.ProgrammingLanguage
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

@OptIn(ExperimentalCoroutinesApi::class)
internal class LessonRepositoryImpl(
    private val remoteDataSource: FirebaseLessonDataSource,
    private val localDataSource: LocalLessonDataSource,
    private val authRepository: AuthRepository,
) : LessonRepository {

    override fun observeLanguages(): Flow<Resource<List<ProgrammingLanguage>>> =
        remoteFirstFlow(
            remote = remoteDataSource::getLanguages,
            fallback = localDataSource::getLanguages,
        )

    override fun observeCategories(
        languageId: String,
    ): Flow<Resource<List<LessonCategory>>> = remoteFirstFlow(
        remote = { remoteDataSource.getCategories(languageId) },
        fallback = { localDataSource.getCategories(languageId) },
    )

    override fun observeLessonsByCategory(
        languageId: String,
        categoryId: String,
    ): Flow<Resource<List<Lesson>>> = authRepository.observeCurrentUser()
        .flatMapLatest { user ->
            lessonFlags(user?.uid).mapLatest { flags ->
                remoteFirst(
                    remote = {
                        remoteDataSource.getLessons(
                            languageId = languageId,
                            categoryId = categoryId,
                            watchedLessonIds = flags.watchedLessonIds,
                            bookmarkedLessonIds = flags.bookmarkedLessonIds,
                        )
                    },
                    fallback = {
                        localDataSource.getLessons(
                            languageId = languageId,
                            categoryId = categoryId,
                            userId = user?.uid,
                        )
                    },
                )
            }
        }
        .asResourceFlow()

    override fun observeLessonsByType(
        languageId: String,
        type: LessonContentType,
    ): Flow<Resource<List<Lesson>>> = authRepository.observeCurrentUser()
        .flatMapLatest { user ->
            lessonFlags(user?.uid).mapLatest { flags ->
                remoteFirst(
                    remote = {
                        remoteDataSource.getLessonsByType(
                            languageId = languageId,
                            type = type,
                            watchedLessonIds = flags.watchedLessonIds,
                            bookmarkedLessonIds = flags.bookmarkedLessonIds,
                        )
                    },
                    fallback = {
                        localDataSource.getLessonsByType(
                            languageId = languageId,
                            type = type,
                            userId = user?.uid,
                        )
                    },
                )
            }
        }
        .asResourceFlow()

    override fun observeLesson(lessonId: String): Flow<Resource<Lesson>> =
        authRepository.observeCurrentUser()
            .flatMapLatest { user ->
                lessonFlags(user?.uid).mapLatest { flags ->
                    remoteFirst(
                        remote = {
                            remoteDataSource.getLesson(
                                lessonId = lessonId,
                                watchedLessonIds = flags.watchedLessonIds,
                                bookmarkedLessonIds = flags.bookmarkedLessonIds,
                            )
                        },
                        fallback = {
                            localDataSource.getLesson(
                                lessonId = lessonId,
                                userId = user?.uid,
                            )
                        },
                    )
                }
            }
            .asResourceFlow()

    override fun observeBookmarkedLessons(languageId: String): Flow<Resource<List<Lesson>>> =
        authRepository.observeCurrentUser()
            .flatMapLatest { user ->
                lessonFlags(user?.uid).mapLatest { flags ->
                    remoteFirst(
                        remote = {
                            remoteDataSource.getBookmarkedLessons(
                                languageId = languageId,
                                watchedLessonIds = flags.watchedLessonIds,
                                bookmarkedLessonIds = flags.bookmarkedLessonIds,
                            )
                        },
                        fallback = {
                            localDataSource.getBookmarkedLessons(
                                languageId = languageId,
                                userId = user?.uid,
                            )
                        },
                    )
                }
            }
            .asResourceFlow()

    override suspend fun markAsWatched(
        userId: String,
        lessonId: String,
    ): Resource<Unit> = runCatching {
        remoteDataSource.markAsWatched(
            userId = userId,
            lessonId = lessonId,
        )
        runCatching {
            localDataSource.markAsWatched(
                userId = userId,
                lessonId = lessonId,
            )
        }
    }.fold(
        onSuccess = { Resource.Success(Unit) },
        onFailure = { error -> error.toResourceError() },
    )

    override suspend fun setBookmarked(
        userId: String,
        lessonId: String,
        bookmarked: Boolean,
    ): Resource<Unit> = runCatching {
        remoteDataSource.setBookmarked(
            userId = userId,
            lessonId = lessonId,
            bookmarked = bookmarked,
        )
        runCatching {
            localDataSource.setBookmarked(
                userId = userId,
                lessonId = lessonId,
                bookmarked = bookmarked,
            )
        }
    }.fold(
        onSuccess = { Resource.Success(Unit) },
        onFailure = { error -> error.toResourceError() },
    )

    private fun lessonFlags(userId: String?): Flow<LessonFlags> =
        if (userId == null) {
            flowOf(LessonFlags())
        } else {
            combine(
                watchedLessonIds(userId),
                bookmarkedLessonIds(userId),
            ) { watchedLessonIds, bookmarkedLessonIds ->
                LessonFlags(
                    watchedLessonIds = watchedLessonIds,
                    bookmarkedLessonIds = bookmarkedLessonIds,
                )
            }
        }

    private fun watchedLessonIds(userId: String?): Flow<Set<String>> =
        if (userId == null) {
            flowOf(emptySet())
        } else {
            remoteDataSource.observeWatchedLessonIds(userId)
                .onStart { emit(emptySet()) }
                .catch { emit(emptySet()) }
        }

    private fun bookmarkedLessonIds(userId: String): Flow<Set<String>> =
        remoteDataSource.observeBookmarkedLessonIds(userId)
            .onStart { emit(emptySet()) }
            .catch { emit(emptySet()) }

    private fun <T> remoteFirstFlow(
        remote: suspend () -> T,
        fallback: suspend () -> T,
    ): Flow<Resource<T>> = flow<Resource<T>> {
        emit(Resource.Loading)
        emit(Resource.Success(remoteFirst(remote, fallback)))
    }.catch { error ->
        emit(error.toResourceError())
    }

    private suspend fun <T> remoteFirst(
        remote: suspend () -> T,
        fallback: suspend () -> T,
    ): T = try {
        withTimeout(REMOTE_TIMEOUT_MS) { remote() }
    } catch (_: TimeoutCancellationException) {
        fallback()
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        fallback()
    }

    private fun <T> Flow<T>.asResourceFlow(): Flow<Resource<T>> =
        map<T, Resource<T>> { value -> Resource.Success(value) }
            .onStart { emit(Resource.Loading) }
            .catch { error -> emit(error.toResourceError()) }

    private fun Throwable.toResourceError() = Resource.Error(
        message = localizedMessage ?: "Unable to load lesson data.",
        cause = this,
    )

    private companion object {
        const val REMOTE_TIMEOUT_MS = 8_000L
    }

    private data class LessonFlags(
        val watchedLessonIds: Set<String> = emptySet(),
        val bookmarkedLessonIds: Set<String> = emptySet(),
    )
}
