package com.eztech.core.domain.model

/**
 * Full payload returned by the recommendation use case.
 *
 * The dashboard keeps both the calculated learning signals and the visible cards together so
 * Home and the dedicated Recommendations page always render the same recommendation state.
 */
data class RecommendationDashboard(
    val stats: RecommendationStats,
    val recommendations: List<Recommendation>,
)

/**
 * Aggregated learning signals used to explain and rank recommendations.
 *
 * These values are derived from the current user profile plus the available problem set, so they
 * update automatically when the user solves more Easy/Medium/Hard problems or enters a new stage.
 */
data class RecommendationStats(
    val solvedProblems: Int,
    val totalProblems: Int,
    val solvedEasy: Int,
    val solvedMedium: Int,
    val solvedHard: Int,
    val currentStage: String,
    val nextStage: String,
    val nextDifficulty: Difficulty,
    val weakestArea: String,
    val stageSolved: Int,
    val stageTotal: Int,
) {
    /** Overall solved percentage shown in the Home recommendation stats card. */
    val progressPercent: Int
        get() = if (totalProblems == 0) 0 else solvedProblems * 100 / totalProblems

    /** Current curriculum-stage progress in compact "solved/total" form. */
    val stageProgressText: String
        get() = "$stageSolved/$stageTotal"
}

/** Small label/value chip attached to recommendation cards for transparent scoring context. */
data class RecommendationMetric(
    val label: String,
    val value: String,
)

/**
 * A single recommendation target.
 *
 * The card can point either to a problem or to a lesson. Extra labels and metrics are intentionally
 * stored here instead of recomputed in UI, keeping Compose components simple and deterministic.
 */
data class Recommendation(
    val id: String,
    val type: RecommendationType,
    val title: String,
    val subtitle: String,
    val reason: String,
    val score: Float,
    val stageLabel: String = "",
    val sequenceLabel: String = "",
    val metrics: List<RecommendationMetric> = emptyList(),
    val problem: Problem? = null,
    val lesson: Lesson? = null,
    val tags: List<String> = emptyList(),
)

/** Distinguishes the navigation behavior when a recommendation card is tapped. */
enum class RecommendationType {
    PROBLEM,
    LESSON,
}
