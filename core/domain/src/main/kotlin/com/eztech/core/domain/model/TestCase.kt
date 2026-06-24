package com.eztech.core.domain.model

data class TestCase(
    val id: String,
    val input: String,
    val expectedOutput: String,
    val isHidden: Boolean,
)

