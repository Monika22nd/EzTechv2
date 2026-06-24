package com.eztech.core.domain.usecase.gamification

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.ExpRewards
import com.eztech.core.domain.model.ProblemCompletion
import com.eztech.core.domain.repository.GamificationRepository

class CompleteProblemUseCase(
    private val gamificationRepository: GamificationRepository,
    private val unlockEligibleBadges: UnlockEligibleBadgesUseCase,
) {
    suspend operator fun invoke(
        userId: String,
        problemId: String,
        difficulty: Difficulty,
        solveDurationSeconds: Int,
    ): Resource<ProblemCompletion> {
        if (userId.isBlank()) return Resource.Error("Sign in to save your progress.")
        if (problemId.isBlank()) return Resource.Error("Problem ID is required.")

        val reward = when (difficulty) {
            Difficulty.EASY -> ExpRewards.SOLVE_EASY
            Difficulty.MEDIUM -> ExpRewards.SOLVE_MEDIUM
            Difficulty.HARD -> ExpRewards.SOLVE_HARD
        }
        return when (
            val completion = gamificationRepository.recordProblemSolved(
                userId = userId,
                problemId = problemId,
                difficulty = difficulty,
                expReward = reward,
                solveDurationSeconds = solveDurationSeconds.coerceAtLeast(0),
            )
        ) {
            is Resource.Success -> {
                if (!completion.data.firstSolve) return completion
                val badges = unlockEligibleBadges(
                    userId = userId,
                    progress = completion.data.progress,
                    solveDurationSeconds = solveDurationSeconds,
                )
                Resource.Success(
                    completion.data.copy(
                        newlyUnlockedBadges = (badges as? Resource.Success)?.data.orEmpty(),
                    ),
                )
            }
            is Resource.Error -> completion
            Resource.Loading -> Resource.Loading
        }
    }
}
