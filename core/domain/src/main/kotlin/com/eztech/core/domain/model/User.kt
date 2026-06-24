package com.eztech.core.domain.model

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val exp: Int,
    val level: Int,
    val badges: List<Badge> = emptyList(),
)

