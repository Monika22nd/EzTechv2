package com.eztech.core.domain.usecase.gamification

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.BadgeCatalog
import com.eztech.core.domain.repository.GamificationRepository

class GetUserBadgesUseCase(
    private val gamificationRepository: GamificationRepository,
) {
    /** Merges catalog badges with user's unlocked state */
    suspend operator fun invoke(userId: String): Resource<List<Badge>> {
        return when (val result = gamificationRepository.getBadges(userId)) {
            is Resource.Success -> {
                val unlockedMap = result.data.associateBy { it.id }
                val merged = BadgeCatalog.ALL.map { catalogBadge ->
                    val unlocked = unlockedMap[catalogBadge.id]
                    if (unlocked != null) {
                        catalogBadge.copy(unlocked = true, unlockedAt = unlocked.unlockedAt)
                    } else {
                        catalogBadge
                    }
                }
                Resource.Success(merged)
            }
            is Resource.Error -> result
            is Resource.Loading -> Resource.Loading
        }
    }
}
