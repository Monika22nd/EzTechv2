package com.eztech.core.domain.usecase.problem

import com.eztech.core.domain.repository.ProblemWorkspaceRepository

class GetCodeDraftUseCase(
    private val repository: ProblemWorkspaceRepository,
) {
    suspend operator fun invoke(
        userId: String,
        problemId: String,
    ) = repository.getCodeDraft(
        userId = userId,
        problemId = problemId,
    )
}
