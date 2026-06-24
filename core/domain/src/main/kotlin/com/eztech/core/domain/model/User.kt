package com.eztech.core.domain.model

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val exp: Int = 0,
    val level: Int = 1,
    val badges: List<Badge> = emptyList(),
    val solvedCount: Int = 0,
    val hardSolvedCount: Int = 0,
    val solvedProblemIds: List<String> = emptyList(),
    val watchedLessonIds: List<String> = emptyList(),
    val currentStreak: Int = 0,
    val lastLoginDate: String = "", // "yyyy-MM-dd"
    val rank: Int = 0,
) {
    val expToNextLevel: Int get() = 500 - (exp % 500)
    val expProgressInLevel: Int get() = exp % 500
    val expProgressFraction: Float get() = expProgressInLevel / 500f
}

/** How much EXP each action rewards */
object ExpRewards {
    const val WATCH_VIDEO = 10
    const val SOLVE_EASY = 20
    const val SOLVE_MEDIUM = 50
    const val SOLVE_HARD = 100
    const val DAILY_LOGIN = 5
}

/** Level = floor(totalExp / 500) + 1 */
fun computeLevel(totalExp: Int): Int = (totalExp / 500) + 1
