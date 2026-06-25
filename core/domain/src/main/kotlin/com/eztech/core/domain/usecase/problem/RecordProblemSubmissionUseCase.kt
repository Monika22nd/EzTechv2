package com.eztech.core.domain.usecase.problem

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.SubmissionResult
import com.eztech.core.domain.repository.ProblemWorkspaceRepository

class RecordProblemSubmissionUseCase(
    private val repository: ProblemWorkspaceRepository,
) {
    suspend operator fun invoke(
        userId: String,
        problemId: String,
        result: SubmissionResult,
    ): Resource<Unit> {
        if (problemId.isBlank()) return Resource.Error("Problem ID is required.")
        return repository.recordSubmission(
            userId = userId,
            problemId = problemId,
            result = result,
        )
    }
}
