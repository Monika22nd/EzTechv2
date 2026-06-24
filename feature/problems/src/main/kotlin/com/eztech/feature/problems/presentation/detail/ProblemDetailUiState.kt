package com.eztech.feature.problems.presentation.detail

import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.TestCase

data class ProblemDetailUiState(
    val problem: Problem? = null,
    val visibleTestCases: List<TestCase> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
