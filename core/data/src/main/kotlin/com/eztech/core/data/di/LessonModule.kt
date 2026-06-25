package com.eztech.core.data.di

import com.eztech.core.data.repository.LessonRepositoryImpl
import com.eztech.core.data.source.local.LocalLessonDataSource
import com.eztech.core.data.source.remote.FirebaseLessonDataSource
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.repository.LessonRepository
import com.eztech.core.domain.usecase.GetBookmarkedLessonsUseCase
import com.eztech.core.domain.usecase.GetLessonCategoriesUseCase
import com.eztech.core.domain.usecase.GetLessonUseCase
import com.eztech.core.domain.usecase.GetLessonsByCategoryUseCase
import com.eztech.core.domain.usecase.GetLessonsByTypeUseCase
import com.eztech.core.domain.usecase.GetProgrammingLanguagesUseCase
import com.eztech.core.domain.usecase.MarkLessonWatchedUseCase
import com.eztech.core.domain.usecase.SetLessonBookmarkedUseCase
import com.eztech.core.domain.usecase.gamification.AwardExpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LessonModule {

    @Provides
    @Singleton
    internal fun provideLessonRepository(
        remoteDataSource: FirebaseLessonDataSource,
        localDataSource: LocalLessonDataSource,
        authRepository: AuthRepository,
    ): LessonRepository = LessonRepositoryImpl(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        authRepository = authRepository,
    )

    @Provides
    fun provideGetProgrammingLanguagesUseCase(
        lessonRepository: LessonRepository,
    ) = GetProgrammingLanguagesUseCase(lessonRepository)

    @Provides
    fun provideGetLessonCategoriesUseCase(
        lessonRepository: LessonRepository,
    ) = GetLessonCategoriesUseCase(lessonRepository)

    @Provides
    fun provideGetLessonsByCategoryUseCase(
        lessonRepository: LessonRepository,
    ) = GetLessonsByCategoryUseCase(lessonRepository)

    @Provides
    fun provideGetLessonsByTypeUseCase(
        lessonRepository: LessonRepository,
    ) = GetLessonsByTypeUseCase(lessonRepository)

    @Provides
    fun provideGetLessonUseCase(
        lessonRepository: LessonRepository,
    ) = GetLessonUseCase(lessonRepository)

    @Provides
    fun provideGetBookmarkedLessonsUseCase(
        lessonRepository: LessonRepository,
    ) = GetBookmarkedLessonsUseCase(lessonRepository)

    @Provides
    fun provideSetLessonBookmarkedUseCase(
        lessonRepository: LessonRepository,
    ) = SetLessonBookmarkedUseCase(lessonRepository)

    @Provides
    fun provideMarkLessonWatchedUseCase(
        lessonRepository: LessonRepository,
        awardExp: AwardExpUseCase,
    ) = MarkLessonWatchedUseCase(
        lessonRepository = lessonRepository,
        awardExp = awardExp,
    )
}
