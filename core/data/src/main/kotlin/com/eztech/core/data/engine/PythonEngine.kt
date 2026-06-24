package com.eztech.core.data.engine

import com.eztech.core.domain.model.CodeExecutionResult

internal interface PythonEngine {
    suspend fun run(
        code: String,
        stdin: String = "",
    ): CodeExecutionResult
}
