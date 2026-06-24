package com.eztech.core.domain.model

data class SubmissionResult(
    val passed: Int,
    val totalTests: Int,
    val failedAt: Int?,
    val output: String,
    val executionTimeMs: Long,
    val status: SubmissionStatus,
    val testResults: List<TestCaseResult>,
) {
    val accepted: Boolean = status == SubmissionStatus.ACCEPTED
}

data class TestCaseResult(
    val testCaseId: String,
    val index: Int,
    val status: TestCaseStatus,
    val input: String?,
    val expectedOutput: String?,
    val actualOutput: String?,
    val errorMessage: String?,
    val executionTimeMs: Long,
    val isHidden: Boolean,
)

enum class SubmissionStatus {
    ACCEPTED,
    WRONG_ANSWER,
    RUNTIME_ERROR,
    TIME_LIMIT_EXCEEDED,
}

enum class TestCaseStatus {
    PASSED,
    WRONG_ANSWER,
    RUNTIME_ERROR,
    TIME_LIMIT_EXCEEDED,
}
