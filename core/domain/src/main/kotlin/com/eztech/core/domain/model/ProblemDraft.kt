package com.eztech.core.domain.model

data class ProblemDraft(
    val problemId: String,
    val code: String,
    val updatedAtMillis: Long = 0L,
)
