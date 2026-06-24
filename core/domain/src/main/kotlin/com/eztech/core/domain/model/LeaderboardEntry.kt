package com.eztech.core.domain.model

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val totalExp: Int,
    val solvedCount: Int,
    val level: Int,
    val currentStreak: Int = 0,
    val isCurrentUser: Boolean = false,
)
