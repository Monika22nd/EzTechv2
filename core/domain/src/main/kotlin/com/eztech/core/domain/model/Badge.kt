package com.eztech.core.domain.model

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val requirement: String,
    val unlocked: Boolean = false,
)

