package com.eztech.core.domain.model

data class SubmissionResult(
    val passed: Int,
    val totalTests: Int,
    val failedAt: Int?,
    val output: String,
    val executionTimeMs: Long,
) {
    val accepted: Boolean = passed == totalTests
}

