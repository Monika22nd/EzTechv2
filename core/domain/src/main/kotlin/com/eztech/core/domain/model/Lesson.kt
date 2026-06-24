package com.eztech.core.domain.model

data class Lesson(
    val id: String,
    val languageId: String,
    val categoryId: String,
    val title: String,
    val videoId: String,
    val order: Int,
    val durationSeconds: Int,
    val description: String = "",
    val sourceName: String = "",
    val thumbnailUrl: String? = null,
    val watched: Boolean = false,
)
