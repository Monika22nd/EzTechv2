package com.eztech.core.domain.usecase

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.ExpRewards
import com.eztech.core.domain.repository.LessonRepository
import com.eztech.core.domain.usecase.gamification.AwardExpUseCase

class MarkLessonWatchedUseCase(
    private val lessonRepository: LessonRepository,
    private val awardExp: AwardExpUseCase,
) {
    suspend operator fun invoke(
        userId: String,
        lessonId: String,
    ): Resource<Unit> {
        val progressResult = lessonRepository.markAsWatched(
            userId = userId,
            lessonId = lessonId,
        )
        if (progressResult is Resource.Error) return progressResult

        return awardExp(
            userId = userId,
            amount = ExpRewards.WATCH_VIDEO,
            reason = "lesson_completed:$lessonId",
        )
    }
}
