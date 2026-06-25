package com.eztech.core.domain.usecase.problem

import com.eztech.core.domain.repository.ProblemWorkspaceRepository

class GetProblemSubmissionHistoryUseCase(
    private val repository: ProblemWorkspaceRepository,
) {
    operator fun invoke(
        userId: String,
        problemId: String,
        limit: Int = 10,
    ) = repository.observeSubmissionHistory(
        userId = userId,
        problemId = problemId,
        limit = limit,
    )
}
