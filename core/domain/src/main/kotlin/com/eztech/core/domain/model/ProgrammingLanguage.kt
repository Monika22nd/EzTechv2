package com.eztech.core.domain.model

data class ProgrammingLanguage(
    val id: String,
    val name: String,
    val description: String = "",
    val iconUrl: String? = null,
    val order: Int = 0,
    val isEnabled: Boolean = true,
)
