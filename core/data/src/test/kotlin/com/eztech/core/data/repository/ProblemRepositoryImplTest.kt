package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.data.source.local.ProblemDataSource
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.TestCase
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ProblemRepositoryImplTest {
    @Test
    fun `observeProblems emits loading then filtered data`() = runBlocking {
        val problems = listOf(problem(id = "easy", difficulty = Difficulty.EASY))
        val dataSource = FakeProblemDataSource(problems = problems)
        val repository = ProblemRepositoryImpl(dataSource)

        val emissions = repository.observeProblems(Difficulty.EASY).toList()

        assertEquals(Resource.Loading, emissions[0])
        assertEquals(Resource.Success(problems), emissions[1])
        assertEquals(null, dataSource.requestedDifficulty)
    }

    @Test
    fun `getProblemById returns success`() = runBlocking {
        val expected = problem(id = "sum")
        val repository = ProblemRepositoryImpl(FakeProblemDataSource(problems = listOf(expected)))

        val result = repository.getProblemById("sum")

        assertEquals(Resource.Success(expected), result)
    }

    @Test
    fun `repository prefers remote data`() = runBlocking {
        val remoteProblem = problem(id = "remote")
        val localProblem = problem(id = "local")
        val repository = ProblemRepositoryImpl(
            remoteDataSource = FakeProblemDataSource(problems = listOf(remoteProblem)),
            localDataSource = FakeProblemDataSource(problems = listOf(localProblem)),
        )

        val result = repository.getProblemById("remote")

        assertEquals(Resource.Success(remoteProblem), result)
    }

    @Test
    fun `repository falls back to local data when remote fails`() = runBlocking {
        val localProblem = problem(id = "offline")
        val repository = ProblemRepositoryImpl(
            remoteDataSource = FakeProblemDataSource(
                failure = IllegalStateException("Firestore unavailable"),
            ),
            localDataSource = FakeProblemDataSource(problems = listOf(localProblem)),
        )

        val result = repository.getProblemById("offline")

        assertEquals(Resource.Success(localProblem), result)
    }

    @Test
    fun `getTestCases returns source error as resource error`() = runBlocking {
        val failure = IllegalArgumentException("Unknown problem")
        val repository = ProblemRepositoryImpl(FakeProblemDataSource(failure = failure))

        val result = repository.getTestCases("missing")

        assertTrue(result is Resource.Error)
        result as Resource.Error
        assertEquals("Unknown problem", result.message)
        assertSame(failure, result.cause)
    }

    private class FakeProblemDataSource(
        private val problems: List<Problem> = emptyList(),
        private val testCases: List<TestCase> = emptyList(),
        private val failure: Throwable? = null,
    ) : ProblemDataSource {
        var requestedDifficulty: Difficulty? = null

        override suspend fun getProblems(difficulty: Difficulty?): List<Problem> {
            failure?.let { throw it }
            requestedDifficulty = difficulty
            return problems
        }

        override suspend fun getProblem(problemId: String): Problem {
            failure?.let { throw it }
            return problems.first { problem -> problem.id == problemId }
        }

        override suspend fun getTestCases(problemId: String): List<TestCase> {
            failure?.let { throw it }
            return testCases
        }
    }

    private fun problem(
        id: String,
        difficulty: Difficulty = Difficulty.MEDIUM,
    ) = Problem(
        id = id,
        title = "Title",
        description = "Description",
        difficulty = difficulty,
        constraints = emptyList(),
        starterCode = "print('todo')",
        solutionCode = "print('done')",
    )
}
