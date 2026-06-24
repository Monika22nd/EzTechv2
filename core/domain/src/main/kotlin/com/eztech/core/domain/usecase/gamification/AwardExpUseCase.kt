package com.eztech.core.domain.usecase.gamification

import com.eztech.core.common.Resource
import com.eztech.core.domain.repository.GamificationRepository

class AwardExpUseCase(
    private val gamificationRepository: GamificationRepository,
) {
    suspend operator fun invoke(
        userId: String,
        amount: Int,
        reason: String,
    ): Resource<Unit> = gamificationRepository.awardExp(
        userId = userId,
        amount = amount,
        reason = reason,
    )
}
