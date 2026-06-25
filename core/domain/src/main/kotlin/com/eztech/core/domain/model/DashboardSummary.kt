package com.eztech.core.domain.model

data class DashboardSummary(
    val userName: String,
    val level: Int,
    val exp: Int,
    val expToNextLevel: Int,
    val expProgressFraction: Float,
    val currentStreak: Int,
    val rank: Int,
    val completedLessonCount: Int,
    val totalLessonCount: Int,
    val videoLessonCount: Int,
    val tutorialLessonCount: Int,
    val solvedProblemCount: Int,
    val totalProblemCount: Int,
    val nextLesson: Lesson?,
    val nextProblem: Problem?,
) {
    val lessonProgressFraction: Float
        get() = completedLessonCount.ratioOf(totalLessonCount)

    val problemProgressFraction: Float
        get() = solvedProblemCount.ratioOf(totalProblemCount)
}

private fun Int.ratioOf(total: Int): Float =
    if (total <= 0) 0f else (toFloat() / total).coerceIn(0f, 1f)
