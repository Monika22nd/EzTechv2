package com.eztech.core.data.source.local

import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.TestCase

internal interface ProblemDataSource {
    suspend fun getProblems(difficulty: Difficulty?): List<Problem>

    suspend fun getProblem(problemId: String): Problem

    suspend fun getTestCases(problemId: String): List<TestCase>
}
