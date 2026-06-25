package com.eztech.core.domain.model

data class Recommendation(
    val id: String,
    val type: RecommendationType,
    val title: String,
    val subtitle: String,
    val reason: String,
    val score: Float,
    val problem: Problem? = null,
    val lesson: Lesson? = null,
    val tags: List<String> = emptyList(),
)

enum class RecommendationType {
    PROBLEM,
    LESSON,
}
