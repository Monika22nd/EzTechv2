package com.eztech.feature.ide.presentation

data class IdeUiState(
    val code: String = DEFAULT_PYTHON_CODE,
    val stdin: String = "",
    val stdout: String = "",
    val stderr: String = "",
    val exitCode: Int? = null,
    val executionTimeMs: Long? = null,
    val isRunning: Boolean = false,
)

internal const val DEFAULT_PYTHON_CODE = """# Write Python and tap Run
message = "Hello from PyQuest!"
print(message)

for number in range(1, 4):
    print(number)
"""
