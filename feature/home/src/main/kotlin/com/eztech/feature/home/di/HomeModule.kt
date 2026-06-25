package com.eztech.feature.home.di

import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.repository.GamificationRepository
import com.eztech.core.domain.repository.LessonRepository
import com.eztech.core.domain.repository.ProblemRepository
import com.eztech.core.domain.repository.UserRepository
import com.eztech.core.domain.usecase.GetDashboardSummaryUseCase
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
}
