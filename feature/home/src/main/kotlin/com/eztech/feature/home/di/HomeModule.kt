package com.eztech.feature.home.di

import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.repository.GamificationRepository
import com.eztech.core.domain.repository.LessonRepository
import com.eztech.core.domain.repository.ProblemRepository
import com.eztech.core.domain.repository.UserRepository
import com.eztech.core.domain.usecase.GetDashboardSummaryUseCase
import com.eztech.core.domain.usecase.recommendation.GetRecommendationsUseCase
import com.eztech.core.domain.usecase.recommendation.RecommendationEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object HomeModule {

    @Provides
    fun provideGetDashboardSummaryUseCase(
        authRepository: AuthRepository,
        userRepository: UserRepository,
        lessonRepository: LessonRepository,
        problemRepository: ProblemRepository,
        gamificationRepository: GamificationRepository,
    ) = GetDashboardSummaryUseCase(
        authRepository = authRepository,
        userRepository = userRepository,
        lessonRepository = lessonRepository,
        problemRepository = problemRepository,
        gamificationRepository = gamificationRepository,
    )

    @Provides
    fun provideRecommendationEngine() = RecommendationEngine()

    @Provides
    fun provideGetRecommendationsUseCase(
        authRepository: AuthRepository,
        userRepository: UserRepository,
        lessonRepository: LessonRepository,
        problemRepository: ProblemRepository,
        recommendationEngine: RecommendationEngine,
    ) = GetRecommendationsUseCase(
        authRepository = authRepository,
        userRepository = userRepository,
        lessonRepository = lessonRepository,
        problemRepository = problemRepository,
        engine = recommendationEngine,
    )
}
