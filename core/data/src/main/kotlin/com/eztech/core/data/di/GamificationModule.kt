package com.eztech.core.data.di

import com.eztech.core.data.repository.GamificationRepositoryImpl
import com.eztech.core.domain.repository.GamificationRepository
import com.eztech.core.domain.usecase.gamification.AwardExpUseCase
import com.eztech.core.domain.usecase.gamification.CheckBadgeUnlockUseCase
import com.eztech.core.domain.usecase.gamification.CompleteProblemUseCase
import com.eztech.core.domain.usecase.gamification.GetLeaderboardUseCase
import com.eztech.core.domain.usecase.gamification.GetUserBadgesUseCase
import com.eztech.core.domain.usecase.gamification.RecordDailyLoginUseCase
import com.eztech.core.domain.usecase.gamification.UnlockEligibleBadgesUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GamificationModule {

    @Provides
    @Singleton
    fun provideGamificationRepository(
        firestore: FirebaseFirestore,
    ): GamificationRepository = GamificationRepositoryImpl(firestore)

    @Provides
    fun provideAwardExpUseCase(repo: GamificationRepository) = AwardExpUseCase(repo)

    @Provides
    fun provideCheckBadgeUnlockUseCase(repo: GamificationRepository) = CheckBadgeUnlockUseCase(repo)

    @Provides
    fun provideGetLeaderboardUseCase(repo: GamificationRepository) = GetLeaderboardUseCase(repo)

    @Provides
    fun provideGetUserBadgesUseCase(repo: GamificationRepository) = GetUserBadgesUseCase(repo)

    @Provides
    fun provideUnlockEligibleBadgesUseCase(
        repo: GamificationRepository,
    ) = UnlockEligibleBadgesUseCase(repo)

    @Provides
    fun provideCompleteProblemUseCase(
        repo: GamificationRepository,
        unlockEligibleBadges: UnlockEligibleBadgesUseCase,
    ) = CompleteProblemUseCase(repo, unlockEligibleBadges)

    @Provides
    fun provideRecordDailyLoginUseCase(
        repo: GamificationRepository,
        unlockEligibleBadges: UnlockEligibleBadgesUseCase,
    ) = RecordDailyLoginUseCase(repo, unlockEligibleBadges)
}
