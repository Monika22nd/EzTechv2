package com.eztech.core.domain.usecase.problem

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.TestCase
import com.eztech.core.domain.repository.ProblemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProblemQueryUseCasesTest {

    @Test
    fun `GetProblemsUseCase delegates difficulty filter`() = runBlocking {
        val repository = FakeProblemRepository()
        val useCase = GetProblemsUseCase(repository)

        val result = useCase(Difficulty.MEDIUM).first()

        assertTrue(result is Resource.Success)
        assertEquals(Difficulty.MEDIUM, repository.requestedDifficulty)
    }

    @Test
    fun `GetProblemDetailUseCase rejects blank id`() = runBlocking {
        val repository = FakeProblemRepository()
        val result = GetProblemDetailUseCase(repository)("  ")

        assertTrue(result is Resource.Error)
        assertFalse(repository.detailRequested)
    }

    @Test
    fun `GetVisibleTestCasesUseCase removes hidden cases`() = runBlocking {
        val repository = FakeProblemRepository()

        val result = GetVisibleTestCasesUseCase(repository)("sum")

        assertTrue(result is Resource.Success)
        val visible = (result as Resource.Success).data
        assertEquals(listOf("visible"), visible.map(TestCase::id))
    }

    private class FakeProblemRepository : ProblemRepository {
        var requestedDifficulty: Difficulty? = null
        var detailRequested = false

        override fun observeProblems(difficulty: Difficulty?): Flow<Resource<List<Problem>>> {
            requestedDifficulty = difficulty
            return flowOf(Resource.Success(emptyList()))
        }

        override suspend fun getProblemById(problemId: String): Resource<Problem> {
            detailRequested = true
            return Resource.Error("Not configured")
        }

        override suspend fun getTestCases(problemId: String): Resource<List<TestCase>> =
            Resource.Success(
                listOf(
                    TestCase("visible", "1", "1", isHidden = false),
                    TestCase("hidden", "2", "2", isHidden = true),
                ),
            )
    }
}
