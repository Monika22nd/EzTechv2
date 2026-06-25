package com.eztech.core.domain.model

data class ProblemSubmission(
    val id: String,
    val problemId: String,
    val status: SubmissionStatus,
    val passed: Int,
    val totalTests: Int,
    val executionTimeMs: Long,
    val submittedAtMillis: Long,
) {
    val accepted: Boolean = status == SubmissionStatus.ACCEPTED
}
