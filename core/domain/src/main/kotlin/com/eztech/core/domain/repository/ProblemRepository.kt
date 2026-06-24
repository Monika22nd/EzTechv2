package com.eztech.core.domain.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.TestCase
import kotlinx.coroutines.flow.Flow

interface ProblemRepository {
    fun observeProblems(difficulty: Difficulty? = null): Flow<Resource<List<Problem>>>

    suspend fun getProblemById(problemId: String): Resource<Problem>

    suspend fun getTestCases(problemId: String): Resource<List<TestCase>>
}
