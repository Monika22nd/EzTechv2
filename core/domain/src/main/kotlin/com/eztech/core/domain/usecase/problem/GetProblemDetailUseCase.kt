package com.eztech.core.domain.usecase.problem

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.repository.ProblemRepository

class GetProblemDetailUseCase(
    private val problemRepository: ProblemRepository,
) {
    suspend operator fun invoke(problemId: String): Resource<Problem> {
        if (problemId.isBlank()) {
            return Resource.Error("Problem ID is required.")
        }
        return problemRepository.getProblemById(problemId)
    }
}
