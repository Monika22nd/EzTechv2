package com.eztech.core.domain.usecase.gamification

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.LeaderboardEntry
import com.eztech.core.domain.repository.GamificationRepository
import kotlinx.coroutines.flow.Flow

class GetLeaderboardUseCase(
    private val gamificationRepository: GamificationRepository,
) {
    operator fun invoke(): Flow<Resource<List<LeaderboardEntry>>> =
        gamificationRepository.observeLeaderboard()
}
