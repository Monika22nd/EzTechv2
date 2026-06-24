package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.data.engine.PythonEngine
import com.eztech.core.domain.model.CodeExecutionResult
import com.eztech.core.domain.repository.CodeExecutionRepository
import javax.inject.Inject

internal class CodeExecutionRepositoryImpl @Inject constructor(
    private val pythonEngine: PythonEngine,
) : CodeExecutionRepository {

    override suspend fun executeCode(
        code: String,
        stdin: String,
    ): Resource<CodeExecutionResult> = try {
        Resource.Success(
            pythonEngine.run(
                code = code,
                stdin = stdin,
            ),
        )
    } catch (exception: Exception) {
        Resource.Error(
            message = exception.message ?: "Unable to run Python code.",
            cause = exception,
        )
    }
}
