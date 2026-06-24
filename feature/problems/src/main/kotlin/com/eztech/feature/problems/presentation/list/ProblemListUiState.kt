package com.eztech.feature.problems.presentation.list

import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem

data class ProblemListUiState(
    val problems: List<Problem> = emptyList(),
    val selectedDifficulty: Difficulty? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
