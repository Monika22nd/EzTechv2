package com.eztech.core.domain.model

data class LeaderboardEntry(
    val rank: Int,
    val user: User,
    val totalExp: Int,
    val solvedCount: Int,
)

