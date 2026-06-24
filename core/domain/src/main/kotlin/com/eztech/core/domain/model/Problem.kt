package com.eztech.core.domain.model

data class Problem(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: Difficulty,
    val constraints: List<String>,
    val starterCode: String,
    val solutionCode: String,
    val tags: List<String> = emptyList(),
    val order: Int = 0,
)
