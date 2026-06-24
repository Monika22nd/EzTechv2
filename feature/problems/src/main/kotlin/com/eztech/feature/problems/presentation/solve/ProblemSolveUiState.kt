package com.eztech.feature.problems.presentation.solve

import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.ProblemCompletion
import com.eztech.core.domain.model.SubmissionResult
import com.eztech.core.domain.model.TestCase

data class ProblemSolveUiState(
    val problem: Problem? = null,
    val visibleTestCases: List<TestCase> = emptyList(),
    val code: String = "",
    val submissionResult: SubmissionResult? = null,
    val completion: ProblemCompletion? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)
