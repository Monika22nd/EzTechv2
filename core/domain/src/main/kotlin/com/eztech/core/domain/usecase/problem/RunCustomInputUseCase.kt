package com.eztech.core.domain.usecase.problem

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.CodeExecutionResult
import com.eztech.core.domain.repository.CodeExecutionRepository

class RunCustomInputUseCase(
    private val codeExecutionRepository: CodeExecutionRepository,
) {
    suspend operator fun invoke(
        code: String,
        stdin: String,
    ): Resource<CodeExecutionResult> {
        if (code.isBlank()) return Resource.Error("Enter Python code before running.")
        return codeExecutionRepository.executeCode(
            code = code,
            stdin = stdin,
        )
    }
}
