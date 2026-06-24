package com.eztech.core.domain.model

data class CodeExecutionResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val executionTimeMs: Long,
) {
    val isSuccess: Boolean = exitCode == 0 && stderr.isBlank()
}

