package com.eztech.core.domain.usecase.gamification

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.DailyLoginResult
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.GamificationProgress
import com.eztech.core.domain.model.LeaderboardEntry
import com.eztech.core.domain.model.ProblemCompletion
import com.eztech.core.domain.repository.GamificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteProblemUseCaseTest {
    @Test
    fun `first easy solve awards exp and unlocks eligible badge`() = runBlocking {
        val repository = FakeGamificationRepository(firstSolve = true)
        val useCase = CompleteProblemUseCase(
            gamificationRepository = repository,
            unlockEligibleBadges = UnlockEligibleBadgesUseCase(repository),
        )

        val result = useCase(
            userId = "user",
            problemId = "problem",
            difficulty = Difficulty.EASY,
            solveDurationSeconds = 180,
        )

        assertTrue(result is Resource.Success)
        result as Resource.Success
        assertEquals(20, result.data.awardedExp)
        assertEquals(listOf("first_blood"), repository.unlockedBadgeIds)
        assertEquals(listOf("first_blood"), result.data.newlyUnlockedBadges.map(Badge::id))
    }

    @Test
    fun `repeated solve does not evaluate badges or award exp`() = runBlocking {
        val repository = FakeGamificationRepository(firstSolve = false)
        val useCase = CompleteProblemUseCase(
            gamificationRepository = repository,
            unlockEligibleBadges = UnlockEligibleBadgesUseCase(repository),
        )

        val result = useCase(
            userId = "user",
            problemId = "problem",
            difficulty = Difficulty.HARD,
            solveDurationSeconds = 30,
        )

        assertTrue(result is Resource.Success)
        result as Resource.Success
        assertFalse(result.data.firstSolve)
        assertEquals(0, result.data.awardedExp)
        assertEquals(0, repository.badgeReadCount)
        assertTrue(repository.unlockedBadgeIds.isEmpty())
    }

    @Test
    fun `daily login already recorded does not evaluate badges`() = runBlocking {
        val repository = FakeGamificationRepository(
            firstSolve = false,
            firstLoginToday = false,
        )
        val useCase = RecordDailyLoginUseCase(
            gamificationRepository = repository,
            unlockEligibleBadges = UnlockEligibleBadgesUseCase(repository),
        )

        val result = useCase("user")

        assertTrue(result is Resource.Success)
        result as Resource.Success
        assertFalse(result.data.firstLoginToday)
        assertEquals(0, result.data.awardedExp)
        assertEquals(0, repository.badgeReadCount)
    }

    private class FakeGamificationRepository(
        private val firstSolve: Boolean,
        private val firstLoginToday: Boolean = false,
    ) : GamificationRepository {
        var badgeReadCount = 0
        val unlockedBadgeIds = mutableListOf<String>()

        private val progress = GamificationProgress(
            totalExp = if (firstSolve) 20 else 100,
            level = 1,
            solvedCount = 1,
            hardSolvedCount = 0,
            currentStreak = 0,
            watchedLessonCount = 0,
        )

        override suspend fun awardExp(userId: String, amount: Int, reason: String) =
            Resource.Success(Unit)

        override suspend fun getBadges(userId: String): Resource<List<Badge>> {
            badgeReadCount++
            return Resource.Success(emptyList())
        }

        override suspend fun unlockBadge(userId: String, badgeId: String): Resource<Unit> {
            unlockedBadgeIds += badgeId
            return Resource.Success(Unit)
        }

        override suspend fun recordProblemSolved(
            userId: String,
            problemId: String,
            difficulty: Difficulty,
            expReward: Int,
            solveDurationSeconds: Int,
        ) = Resource.Success(
            ProblemCompletion(
                firstSolve = firstSolve,
                awardedExp = if (firstSolve) expReward else 0,
                progress = progress,
            ),
        )

        override suspend fun recordDailyLogin(
            userId: String,
            today: String,
            yesterday: String,
            expReward: Int,
        ) = Resource.Success(
            DailyLoginResult(
                firstLoginToday = firstLoginToday,
                awardedExp = if (firstLoginToday) expReward else 0,
                progress = progress,
            ),
        )

        override fun observeLeaderboard(): Flow<Resource<List<LeaderboardEntry>>> =
            flowOf(Resource.Success(emptyList()))

        override fun observeUserLeaderboardEntry(
            userId: String,
        ): Flow<Resource<LeaderboardEntry?>> = flowOf(Resource.Success(null))
    }
}
