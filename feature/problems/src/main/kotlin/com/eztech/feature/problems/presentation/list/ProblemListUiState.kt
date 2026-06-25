package com.eztech.feature.problems.presentation.list

import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem

data class ProblemListUiState(
    val allProblems: List<Problem> = emptyList(),
    val problems: List<Problem> = emptyList(),
    val searchQuery: String = "",
    val selectedDifficulty: Difficulty? = null,
    val selectedTag: String? = null,
    val availableTags: List<String> = emptyList(),
    val sortOption: ProblemSortOption = ProblemSortOption.ORDER,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

enum class ProblemSortOption(val label: String) {
    ORDER("Order"),
    EASY_FIRST("Easy first"),
    HARD_FIRST("Hard first"),
}
