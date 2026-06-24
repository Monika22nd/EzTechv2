package com.eztech.core.domain.usecase.problem

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.repository.ProblemRepository
import kotlinx.coroutines.flow.Flow

class GetProblemsUseCase(
    private val problemRepository: ProblemRepository,
) {
    operator fun invoke(
        difficulty: Difficulty? = null,
    ): Flow<Resource<List<Problem>>> = problemRepository.observeProblems(difficulty)
}
