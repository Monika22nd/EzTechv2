package com.eztech.core.domain.usecase.problem

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.CodeExecutionResult
import com.eztech.core.domain.model.SubmissionResult
import com.eztech.core.domain.model.SubmissionStatus
import com.eztech.core.domain.model.TestCase
import com.eztech.core.domain.model.TestCaseResult
import com.eztech.core.domain.model.TestCaseStatus
import com.eztech.core.domain.repository.CodeExecutionRepository
import com.eztech.core.domain.repository.ProblemRepository

class SubmitSolutionUseCase(
    private val problemRepository: ProblemRepository,
    private val codeExecutionRepository: CodeExecutionRepository,
) {
    suspend operator fun invoke(
        problemId: String,
        code: String,
    ): Resource<SubmissionResult> {
        if (problemId.isBlank()) {
            return Resource.Error("Problem ID is required.")
        }
        if (code.isBlank()) {
            return Resource.Error("Enter Python code before submitting.")
        }

        val testCases = when (val result = problemRepository.getTestCases(problemId)) {
            is Resource.Success -> result.data
            is Resource.Error -> return result
            Resource.Loading -> return Resource.Error("Test cases are still loading.")
        }
        if (testCases.isEmpty()) {
            return Resource.Error("This problem does not have any test cases.")
        }

        val results = mutableListOf<TestCaseResult>()
        var firstFailedAt: Int? = null
        var firstFailureStatus: TestCaseStatus? = null
        var totalExecutionTimeMs = 0L

        for ((index, testCase) in testCases.withIndex()) {
            val execution = when (
                val result = codeExecutionRepository.executeCode(
                    code = testCase.executableCode(code),
                    stdin = testCase.executionStdin(),
                )
            ) {
                is Resource.Success -> result.data
                is Resource.Error -> return Resource.Error(
                    message = result.message,
                    cause = result.cause,
                )
                Resource.Loading -> return Resource.Error("Python execution is still loading.")
            }

            totalExecutionTimeMs += execution.executionTimeMs
            val status = classify(execution, testCase)
            val testResult = testCase.toResult(
                index = index,
                execution = execution,
                status = status,
            )
            results += testResult

            if (status != TestCaseStatus.PASSED && firstFailedAt == null) {
                firstFailedAt = index
                firstFailureStatus = status
            }

            if (
                status == TestCaseStatus.RUNTIME_ERROR ||
                status == TestCaseStatus.TIME_LIMIT_EXCEEDED
            ) {
                break
            }
        }

        val passedCount = results.count { it.status == TestCaseStatus.PASSED }
        val submissionStatus = firstFailureStatus.toSubmissionStatus()
        val firstFailure = firstFailedAt?.let { failedIndex ->
            results.firstOrNull { it.index == failedIndex }
        }

        return Resource.Success(
            SubmissionResult(
                passed = passedCount,
                totalTests = testCases.size,
                failedAt = firstFailedAt,
                output = firstFailure?.actualOutput
                    ?: firstFailure?.errorMessage
                    ?: results.lastOrNull()?.actualOutput.orEmpty(),
                executionTimeMs = totalExecutionTimeMs,
                status = submissionStatus,
                testResults = results,
            ),
        )
    }

    private fun classify(
        execution: CodeExecutionResult,
        testCase: TestCase,
    ): TestCaseStatus = when {
        execution.isTimeout() -> TestCaseStatus.TIME_LIMIT_EXCEEDED
        testCase.isAssertionHarness() && execution.isAssertionFailure() -> TestCaseStatus.WRONG_ANSWER
        !execution.isSuccess -> TestCaseStatus.RUNTIME_ERROR
        testCase.isAssertionHarness() -> TestCaseStatus.PASSED
        normalizeOutput(execution.stdout) == normalizeOutput(testCase.expectedOutput) ->
            TestCaseStatus.PASSED
        else -> TestCaseStatus.WRONG_ANSWER
    }

    private fun TestCase.toResult(
        index: Int,
        execution: CodeExecutionResult,
        status: TestCaseStatus,
    ): TestCaseResult {
        val hideDetails = isHidden && status != TestCaseStatus.PASSED
        return TestCaseResult(
            testCaseId = id,
            index = index,
            status = status,
            input = input.takeUnless { isHidden },
            expectedOutput = displayExpectedOutput().takeUnless { isHidden },
            actualOutput = execution.stdout.takeUnless { hideDetails },
            errorMessage = when {
                hideDetails -> "Hidden test failed."
                execution.stderr.isNotBlank() -> execution.stderr
                else -> null
            },
            executionTimeMs = execution.executionTimeMs,
            isHidden = isHidden,
        )
    }

    private fun CodeExecutionResult.isTimeout(): Boolean =
        stderr.contains("TimeoutError", ignoreCase = true) ||
            stderr.contains("time limit", ignoreCase = true)

    private fun CodeExecutionResult.isAssertionFailure(): Boolean =
        !isSuccess && stderr.contains("AssertionError", ignoreCase = true)

    private fun TestCase.executableCode(userCode: String): String =
        if (isAssertionHarness()) {
            buildString {
                append(userCode.trimEnd())
                append("\n\n")
                append(input)
            }
        } else {
            userCode
        }

    private fun TestCase.executionStdin(): String =
        if (isAssertionHarness()) "" else input

    private fun TestCase.isAssertionHarness(): Boolean =
        input.lineSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .any { line -> line.startsWith("assert ") }

    private fun TestCase.displayExpectedOutput(): String =
        if (isAssertionHarness()) "Assertion passes" else expectedOutput

    private fun normalizeOutput(output: String): String = output
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .lines()
        .joinToString("\n", transform = String::trimEnd)
        .trim()

    private fun TestCaseStatus?.toSubmissionStatus(): SubmissionStatus = when (this) {
        null -> SubmissionStatus.ACCEPTED
        TestCaseStatus.PASSED -> SubmissionStatus.ACCEPTED
        TestCaseStatus.WRONG_ANSWER -> SubmissionStatus.WRONG_ANSWER
        TestCaseStatus.RUNTIME_ERROR -> SubmissionStatus.RUNTIME_ERROR
        TestCaseStatus.TIME_LIMIT_EXCEEDED -> SubmissionStatus.TIME_LIMIT_EXCEEDED
    }
}
