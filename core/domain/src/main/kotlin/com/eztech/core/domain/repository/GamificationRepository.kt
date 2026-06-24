package com.eztech.core.domain.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow

interface GamificationRepository {
    suspend fun awardExp(
        userId: String,
        amount: Int,
        reason: String,
    ): Resource<Unit>

    suspend fun getBadges(userId: String): Resource<List<Badge>>

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>>
}

