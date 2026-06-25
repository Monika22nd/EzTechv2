package com.eztech.core.domain.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.ProblemDraft
import com.eztech.core.domain.model.ProblemSubmission
import com.eztech.core.domain.model.SubmissionResult
import kotlinx.coroutines.flow.Flow

interface ProblemWorkspaceRepository {
    suspend fun getCodeDraft(
        userId: String,
        problemId: String,
    ): Resource<ProblemDraft?>

    suspend fun saveCodeDraft(
        userId: String,
        problemId: String,
        code: String,
    ): Resource<Unit>

    suspend fun recordSubmission(
        userId: String,
        problemId: String,
        result: SubmissionResult,
    ): Resource<Unit>

    fun observeSubmissionHistory(
        userId: String,
        problemId: String,
        limit: Int = 10,
    ): Flow<Resource<List<ProblemSubmission>>>
}
