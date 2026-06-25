package com.eztech.core.domain.model

data class Lesson(
    val id: String,
    val languageId: String,
    val categoryId: String,
    val title: String,
    val videoId: String = "",
    val order: Int,
    val durationSeconds: Int,
    val description: String = "",
    val content: String = "",
    val type: LessonContentType = LessonContentType.VIDEO,
    val sourceName: String = "",
    val thumbnailUrl: String? = null,
    val watched: Boolean = false,
    val bookmarked: Boolean = false,
)
