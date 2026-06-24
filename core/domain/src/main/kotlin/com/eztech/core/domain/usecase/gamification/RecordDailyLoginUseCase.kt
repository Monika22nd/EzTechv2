package com.eztech.core.domain.usecase.gamification

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.ExpRewards
import com.eztech.core.domain.repository.GamificationRepository
import java.time.LocalDate

class RecordDailyLoginUseCase(
    private val gamificationRepository: GamificationRepository,
) {
    suspend operator fun invoke(
        userId: String,
        lastLoginDate: String,
    ): Resource<Boolean> {
        val today = LocalDate.now().toString()
        if (lastLoginDate == today) return Resource.Success(false) // already logged in today

        val result = gamificationRepository.recordDailyLogin(userId = userId, today = today)
        return if (result is Resource.Success) {
            // Award daily EXP
            gamificationRepository.awardExp(
                userId = userId,
                amount = ExpRewards.DAILY_LOGIN,
                reason = "daily_login",
            )
            Resource.Success(true)
        } else {
            Resource.Success(false)
        }
    }
}
