package com.eztech.feature.problems.presentation.list

import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.feature.problems.presentation.model.ProblemTypeFilter

data class ProblemListUiState(
    val allProblems: List<Problem> = emptyList(),
    val problems: List<Problem> = emptyList(),
    val searchQuery: String = "",
    val selectedDifficulty: Difficulty? = null,
    val selectedProblemType: String? = null,
    val availableProblemTypes: List<ProblemTypeFilter> = emptyList(),
    val sortOption: ProblemSortOption = ProblemSortOption.ORDER,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

enum class ProblemSortOption(val label: String) {
    ORDER("Order"),
    EASY_FIRST("Easy first"),
    HARD_FIRST("Hard first"),
}
