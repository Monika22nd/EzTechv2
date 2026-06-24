package com.eztech.core.domain.usecase.gamification

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.BadgeCatalog
import com.eztech.core.domain.model.BadgeRequirementType
import com.eztech.core.domain.model.GamificationProgress
import com.eztech.core.domain.repository.GamificationRepository

class UnlockEligibleBadgesUseCase(
    private val gamificationRepository: GamificationRepository,
) {
    suspend operator fun invoke(
        userId: String,
        progress: GamificationProgress,
        solveDurationSeconds: Int? = null,
    ): Resource<List<Badge>> {
        val unlockedIds = when (val result = gamificationRepository.getBadges(userId)) {
            is Resource.Success -> result.data.filter(Badge::unlocked).map(Badge::id).toSet()
            is Resource.Error -> return result
            Resource.Loading -> return Resource.Loading
        }

        val newlyUnlocked = mutableListOf<Badge>()
        BadgeCatalog.ALL
            .asSequence()
            .filterNot { badge -> badge.id in unlockedIds }
            .filter { badge -> badge.isEligible(progress, solveDurationSeconds) }
            .forEach { badge ->
                if (gamificationRepository.unlockBadge(userId, badge.id) is Resource.Success) {
                    newlyUnlocked += badge.copy(
                        unlocked = true,
                        unlockedAt = System.currentTimeMillis(),
                    )
                }
            }
        return Resource.Success(newlyUnlocked)
    }

    private fun Badge.isEligible(
        progress: GamificationProgress,
        solveDurationSeconds: Int?,
    ): Boolean = when (requirement.type) {
        BadgeRequirementType.SOLVE_COUNT -> progress.solvedCount >= requirement.value
        BadgeRequirementType.EXP -> progress.totalExp >= requirement.value
        BadgeRequirementType.STREAK -> progress.currentStreak >= requirement.value
        BadgeRequirementType.WATCH_COUNT -> progress.watchedLessonCount >= requirement.value
        BadgeRequirementType.HARD_COUNT -> progress.hardSolvedCount >= requirement.value
        BadgeRequirementType.LEVEL -> progress.level >= requirement.value
        BadgeRequirementType.SPEED_SOLVE ->
            solveDurationSeconds != null && solveDurationSeconds <= requirement.value
    }
}
