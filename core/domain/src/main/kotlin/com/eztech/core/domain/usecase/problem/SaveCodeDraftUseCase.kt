package com.eztech.core.domain.usecase.problem

import com.eztech.core.common.Resource
import com.eztech.core.domain.repository.ProblemWorkspaceRepository

class SaveCodeDraftUseCase(
    private val repository: ProblemWorkspaceRepository,
) {
    suspend operator fun invoke(
        userId: String,
        problemId: String,
        code: String,
    ): Resource<Unit> {
        if (problemId.isBlank()) return Resource.Error("Problem ID is required.")
        return repository.saveCodeDraft(
            userId = userId,
            problemId = problemId,
            code = code,
        )
    }
}
