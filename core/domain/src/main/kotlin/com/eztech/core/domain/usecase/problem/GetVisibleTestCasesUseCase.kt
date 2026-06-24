package com.eztech.core.domain.usecase.problem

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.TestCase
import com.eztech.core.domain.repository.ProblemRepository

class GetVisibleTestCasesUseCase(
    private val problemRepository: ProblemRepository,
) {
    suspend operator fun invoke(problemId: String): Resource<List<TestCase>> {
        if (problemId.isBlank()) {
            return Resource.Error("Problem ID is required.")
        }

        return when (val result = problemRepository.getTestCases(problemId)) {
            is Resource.Success -> Resource.Success(
                result.data.filterNot(TestCase::isHidden),
            )
            is Resource.Error -> result
            Resource.Loading -> Resource.Loading
        }
    }
}
