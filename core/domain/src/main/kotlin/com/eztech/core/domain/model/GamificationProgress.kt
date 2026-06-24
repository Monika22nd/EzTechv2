package com.eztech.core.domain.model

data class GamificationProgress(
    val totalExp: Int,
    val level: Int,
    val solvedCount: Int,
    val hardSolvedCount: Int,
    val currentStreak: Int,
    val watchedLessonCount: Int,
)

data class ProblemCompletion(
    val firstSolve: Boolean,
    val awardedExp: Int,
    val progress: GamificationProgress,
    val newlyUnlockedBadges: List<Badge> = emptyList(),
)

data class DailyLoginResult(
    val firstLoginToday: Boolean,
    val awardedExp: Int,
    val progress: GamificationProgress,
    val newlyUnlockedBadges: List<Badge> = emptyList(),
)
