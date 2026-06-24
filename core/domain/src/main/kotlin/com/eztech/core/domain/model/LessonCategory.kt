package com.eztech.core.domain.model

data class LessonCategory(
    val id: String,
    val languageId: String,
    val name: String,
    val lessonCount: Int,
    val description: String = "",
    val type: LessonContentType = LessonContentType.TUTORIAL,
    val iconUrl: String? = null,
    val order: Int = 0,
)
