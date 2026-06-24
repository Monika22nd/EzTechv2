package com.eztech.core.domain.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.CodeExecutionResult

interface CodeExecutionRepository {
    suspend fun executeCode(
        code: String,
        stdin: String = "",
    ): Resource<CodeExecutionResult>
}

