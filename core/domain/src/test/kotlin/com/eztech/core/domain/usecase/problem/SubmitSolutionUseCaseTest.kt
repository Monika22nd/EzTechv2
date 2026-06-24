package com.eztech.core.domain.usecase.problem

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.CodeExecutionResult
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.SubmissionStatus
import com.eztech.core.domain.model.TestCase
import com.eztech.core.domain.model.TestCaseStatus
import com.eztech.core.domain.repository.CodeExecutionRepository
import com.eztech.core.domain.repository.ProblemRepository
import java.util.ArrayDeque
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmitSolutionUseCaseTest {

    @Test
    fun `blank code returns error without loading tests`() = runBlocking {
        val problems = FakeProblemRepository(Resource.Success(listOf(testCase("1", "", ""))))
        val executions = FakeCodeExecutionRepository()
        val useCase = SubmitSolutionUseCase(problems, executions)

        val result = useCase(problemId = "sum", code = "  \n")

        assertTrue(result is Resource.Error)
        assertEquals(0, problems.testCaseRequests)
        assertEquals(0, executions.requests.size)
    }

    @Test
    fun `all matching outputs return accepted result`() = runBlocking {
        val problems = FakeProblemRepository(
            Resource.Success(
                listOf(
                    testCase("one", "1\n", "2\n"),
                    testCase("two", "5\n", "10\n"),
                ),
            ),
        )
        val executions = FakeCodeExecutionRepository(
            success(stdout = "2\r\n", timeMs = 4),
            success(stdout = "10  \n", timeMs = 7),
        )
        val useCase = SubmitSolutionUseCase(problems, executions)

        val result = useCase("double", "print(int(input()) * 2)")

        assertTrue(result is Resource.Success)
        val submission = (result as Resource.Success).data
        assertTrue(submission.accepted)
        assertEquals(SubmissionStatus.ACCEPTED, submission.status)
        assertEquals(2, submission.passed)
        assertEquals(2, submission.totalTests)
        assertNull(submission.failedAt)
        assertEquals(11L, submission.executionTimeMs)
        assertEquals(listOf("1\n", "5\n"), executions.requests.map { it.second })
    }

    @Test
    fun `wrong answers continue and keep first failed index`() = runBlocking {
        val problems = FakeProblemRepository(
            Resource.Success(
                listOf(
                    testCase("visible-fail", "1\n", "2\n"),
                    testCase("pass", "2\n", "4\n"),
                    testCase("hidden-fail", "9\n", "18\n", isHidden = true),
                ),
            ),
        )
        val executions = FakeCodeExecutionRepository(
            success("3\n"),
            success("4\n"),
            success("19\n"),
        )
        val useCase = SubmitSolutionUseCase(problems, executions)

        val result = useCase("double", "print(int(input()) * 2)")

        val submission = (result as Resource.Success).data
        assertFalse(submission.accepted)
        assertEquals(SubmissionStatus.WRONG_ANSWER, submission.status)
        assertEquals(1, submission.passed)
        assertEquals(0, submission.failedAt)
        assertEquals(3, submission.testResults.size)
        assertEquals(TestCaseStatus.WRONG_ANSWER, submission.testResults[2].status)
        assertNull(submission.testResults[2].input)
        assertNull(submission.testResults[2].expectedOutput)
        assertNull(submission.testResults[2].actualOutput)
        assertEquals("Hidden test failed.", submission.testResults[2].errorMessage)
    }

    @Test
    fun `assertion harness appends test code and ignores stdout comparison`() = runBlocking {
        val problems = FakeProblemRepository(
            Resource.Success(
                listOf(
                    testCase(
                        id = "assertion",
                        input = "assert double(4) == 8",
                        expected = "Assertion passes",
                    ),
                ),
            ),
        )
        val executions = FakeCodeExecutionRepository(success(stdout = "debug output\n"))
        val useCase = SubmitSolutionUseCase(problems, executions)

        val result = useCase("double", "def double(value):\n    return value * 2")

        val submission = (result as Resource.Success).data
        assertTrue(submission.accepted)
        assertEquals("", executions.requests.single().second)
        assertTrue(executions.requests.single().first.contains("assert double(4) == 8"))
        assertEquals("Assertion passes", submission.testResults.single().expectedOutput)
    }

    @Test
    fun `assertion failure is classified as wrong answer`() = runBlocking {
        val problems = FakeProblemRepository(
            Resource.Success(
                listOf(
                    testCase("visible", "assert double(4) == 8", "Assertion passes"),
                    testCase("next", "assert double(5) == 10", "Assertion passes"),
                ),
            ),
        )
        val executions = FakeCodeExecutionRepository(
            Resource.Success(
                CodeExecutionResult(
                    stdout = "",
                    stderr = "AssertionError",
                    exitCode = 1,
                    executionTimeMs = 1,
                ),
            ),
            success(stdout = ""),
        )

        val result = SubmitSolutionUseCase(problems, executions)(
            problemId = "double",
            code = "def double(value):\n    return value",
        )

        val submission = (result as Resource.Success).data
        assertEquals(SubmissionStatus.WRONG_ANSWER, submission.status)
        assertEquals(TestCaseStatus.WRONG_ANSWER, submission.testResults.first().status)
        assertEquals(2, submission.testResults.size)
    }

    @Test
    fun `runtime error stops remaining executions`() = runBlocking {
        val problems = FakeProblemRepository(
            Resource.Success(
                listOf(
                    testCase("pass", "1\n", "1\n"),
                    testCase("crash", "0\n", "0\n"),
                    testCase("not-run", "2\n", "2\n"),
                ),
            ),
        )
        val executions = FakeCodeExecutionRepository(
            success("1\n", timeMs = 2),
            Resource.Success(
                CodeExecutionResult(
                    stdout = "",
                    stderr = "ZeroDivisionError: division by zero",
                    exitCode = 1,
                    executionTimeMs = 3,
                ),
            ),
        )
        val useCase = SubmitSolutionUseCase(problems, executions)

        val result = useCase("divide", "print(1 / int(input()))")

        val submission = (result as Resource.Success).data
        assertEquals(SubmissionStatus.RUNTIME_ERROR, submission.status)
        assertEquals(1, submission.failedAt)
        assertEquals(1, submission.passed)
        assertEquals(2, submission.testResults.size)
        assertEquals(2, executions.requests.size)
        assertEquals(5L, submission.executionTimeMs)
    }

    @Test
    fun `timeout is classified separately`() = runBlocking {
        val problems = FakeProblemRepository(
            Resource.Success(listOf(testCase("timeout", "", "done"))),
        )
        val executions = FakeCodeExecutionRepository(
            Resource.Success(
                CodeExecutionResult(
                    stdout = "",
                    stderr = "TimeoutError: Execution exceeded the 10 second time limit.",
                    exitCode = 1,
                    executionTimeMs = 10_000,
                ),
            ),
        )

        val result = SubmitSolutionUseCase(problems, executions)("loop", "while True: pass")

        val submission = (result as Resource.Success).data
        assertEquals(SubmissionStatus.TIME_LIMIT_EXCEEDED, submission.status)
        assertEquals(TestCaseStatus.TIME_LIMIT_EXCEEDED, submission.testResults.single().status)
    }

    private fun testCase(
        id: String,
        input: String,
        expected: String,
        isHidden: Boolean = false,
    ) = TestCase(
        id = id,
        input = input,
        expectedOutput = expected,
        isHidden = isHidden,
    )

    private fun success(
        stdout: String,
        timeMs: Long = 1,
    ): Resource<CodeExecutionResult> = Resource.Success(
        CodeExecutionResult(
            stdout = stdout,
            stderr = "",
            exitCode = 0,
            executionTimeMs = timeMs,
        ),
    )

    private class FakeProblemRepository(
        private val testCases: Resource<List<TestCase>>,
    ) : ProblemRepository {
        var testCaseRequests = 0

        override fun observeProblems(difficulty: Difficulty?): Flow<Resource<List<Problem>>> =
            flowOf(Resource.Success(emptyList()))

        override suspend fun getProblemById(problemId: String): Resource<Problem> =
            Resource.Error("Not used")

        override suspend fun getTestCases(problemId: String): Resource<List<TestCase>> {
            testCaseRequests += 1
            return testCases
        }
    }

    private class FakeCodeExecutionRepository(
        vararg results: Resource<CodeExecutionResult>,
    ) : CodeExecutionRepository {
        private val pendingResults = ArrayDeque(results.toList())
        val requests = mutableListOf<Pair<String, String>>()

        override suspend fun executeCode(
            code: String,
            stdin: String,
        ): Resource<CodeExecutionResult> {
            requests += code to stdin
            return pendingResults.removeFirst()
        }
    }
}
