package com.eztech.core.domain.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.DailyLoginResult
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.LeaderboardEntry
import com.eztech.core.domain.model.ProblemCompletion
import kotlinx.coroutines.flow.Flow

interface GamificationRepository {
    suspend fun awardExp(
        userId: String,
        amount: Int,
        reason: String,
    ): Resource<Unit>

    suspend fun getBadges(userId: String): Resource<List<Badge>>

    suspend fun unlockBadge(
        userId: String,
        badgeId: String,
    ): Resource<Unit>

    suspend fun recordProblemSolved(
        userId: String,
        problemId: String,
        difficulty: Difficulty,
        expReward: Int,
        solveDurationSeconds: Int,
    ): Resource<ProblemCompletion>

    suspend fun recordDailyLogin(
        userId: String,
        today: String,
        yesterday: String,
        expReward: Int,
    ): Resource<DailyLoginResult>

    fun observeLeaderboard(): Flow<Resource<List<LeaderboardEntry>>>

    fun observeUserLeaderboardEntry(userId: String): Flow<Resource<LeaderboardEntry?>>
}
