package com.eztech.core.domain.usecase.gamification

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.BadgeCatalog
import com.eztech.core.domain.model.BadgeRequirementType
import com.eztech.core.domain.model.User
import com.eztech.core.domain.repository.GamificationRepository

/**
 * Checks all badges against the user's current stats and unlocks
 * any that haven't been unlocked yet. Returns newly unlocked badges.
 */
class CheckBadgeUnlockUseCase(
    private val gamificationRepository: GamificationRepository,
) {
    suspend operator fun invoke(
        user: User,
        fastSolveSeconds: Int? = null, // pass non-null when checking speed badges
    ): Resource<List<Badge>> {
        val unlockedIds = user.badges.filter { it.unlocked }.map { it.id }.toSet()
        val newlyUnlocked = mutableListOf<Badge>()

        for (badge in BadgeCatalog.ALL) {
            if (badge.id in unlockedIds) continue

            val shouldUnlock = when (badge.requirement.type) {
                BadgeRequirementType.SOLVE_COUNT ->
                    user.solvedCount >= badge.requirement.value
                BadgeRequirementType.EXP ->
                    user.exp >= badge.requirement.value
                BadgeRequirementType.STREAK ->
                    user.currentStreak >= badge.requirement.value
                BadgeRequirementType.WATCH_COUNT ->
                    user.watchedLessonIds.size >= badge.requirement.value
                BadgeRequirementType.HARD_COUNT ->
                    user.hardSolvedCount >= badge.requirement.value
                BadgeRequirementType.LEVEL ->
                    user.level >= badge.requirement.value
                BadgeRequirementType.SPEED_SOLVE ->
                    fastSolveSeconds != null && fastSolveSeconds <= badge.requirement.value
            }

            if (shouldUnlock) {
                val result = gamificationRepository.unlockBadge(
                    userId = user.uid,
                    badgeId = badge.id,
                )
                if (result is Resource.Success) {
                    newlyUnlocked.add(badge.copy(unlocked = true, unlockedAt = System.currentTimeMillis()))
                }
            }
        }

        return Resource.Success(newlyUnlocked)
    }
}
