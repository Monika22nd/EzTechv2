package com.eztech.core.domain.usecase.gamification

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.DailyLoginResult
import com.eztech.core.domain.model.ExpRewards
import com.eztech.core.domain.repository.GamificationRepository
import java.time.LocalDate

class RecordDailyLoginUseCase(
    private val gamificationRepository: GamificationRepository,
    private val unlockEligibleBadges: UnlockEligibleBadgesUseCase,
) {
    suspend operator fun invoke(userId: String): Resource<DailyLoginResult> {
        if (userId.isBlank()) return Resource.Error("User ID is required.")

        val today = LocalDate.now()
        return when (
            val result = gamificationRepository.recordDailyLogin(
                userId = userId,
                today = today.toString(),
                yesterday = today.minusDays(1).toString(),
                expReward = ExpRewards.DAILY_LOGIN,
            )
        ) {
            is Resource.Success -> {
                if (!result.data.firstLoginToday) return result
                val badges = unlockEligibleBadges(
                    userId = userId,
                    progress = result.data.progress,
                )
                Resource.Success(
                    result.data.copy(
                        newlyUnlockedBadges = (badges as? Resource.Success)?.data.orEmpty(),
                    ),
                )
            }
            is Resource.Error -> result
            Resource.Loading -> Resource.Loading
        }
    }
}
