package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.ProblemDraft
import com.eztech.core.domain.model.ProblemSubmission
import com.eztech.core.domain.model.SubmissionResult
import com.eztech.core.domain.model.SubmissionStatus
import com.eztech.core.domain.repository.ProblemWorkspaceRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

internal class ProblemWorkspaceRepositoryImpl(
    private val firestore: FirebaseFirestore,
) : ProblemWorkspaceRepository {

    override suspend fun getCodeDraft(
        userId: String,
        problemId: String,
    ): Resource<ProblemDraft?> = runCatching {
        val snapshot = userDocument(userId)
            .collection(PROBLEM_DRAFTS)
            .document(problemId)
            .get()
            .await()

        snapshot.takeIf(DocumentSnapshot::exists)?.toDraft()
    }.fold(
        onSuccess = { draft -> Resource.Success(draft) },
        onFailure = { error -> error.toResourceError("Unable to load saved draft.") },
    )

    override suspend fun saveCodeDraft(
        userId: String,
        problemId: String,
        code: String,
    ): Resource<Unit> = runCatching {
        userDocument(userId)
            .collection(PROBLEM_DRAFTS)
            .document(problemId)
            .set(
                mapOf(
                    PROBLEM_ID to problemId,
                    CODE to code,
                    UPDATED_AT to Timestamp.now(),
                ),
                SetOptions.merge(),
            )
            .await()
    }.fold(
        onSuccess = { Resource.Success(Unit) },
        onFailure = { error -> error.toResourceError("Unable to save code draft.") },
    )

    override suspend fun recordSubmission(
        userId: String,
        problemId: String,
        result: SubmissionResult,
    ): Resource<Unit> = runCatching {
        userDocument(userId)
            .collection(PROBLEM_SUBMISSIONS)
            .document(problemId)
            .collection(SUBMISSION_ITEMS)
            .document()
            .set(
                mapOf(
                    PROBLEM_ID to problemId,
                    STATUS to result.status.name,
                    PASSED to result.passed,
                    TOTAL_TESTS to result.totalTests,
                    EXECUTION_TIME_MS to result.executionTimeMs,
                    SUBMITTED_AT to Timestamp.now(),
                ),
            )
            .await()
    }.fold(
        onSuccess = { Resource.Success(Unit) },
        onFailure = { error -> error.toResourceError("Unable to save submission history.") },
    )

    override fun observeSubmissionHistory(
        userId: String,
        problemId: String,
        limit: Int,
    ): Flow<Resource<List<ProblemSubmission>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = userDocument(userId)
            .collection(PROBLEM_SUBMISSIONS)
            .document(problemId)
            .collection(SUBMISSION_ITEMS)
            .orderBy(SUBMITTED_AT, Query.Direction.DESCENDING)
            .limit(limit.coerceAtLeast(1).toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(error.toResourceError("Unable to load submission history."))
                    return@addSnapshotListener
                }
                val submissions = snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toSubmission(problemId = problemId)
                }
                trySend(Resource.Success(submissions))
            }

        awaitClose(registration::remove)
    }

    private fun userDocument(userId: String) =
        firestore.collection(USERS).document(userId)

    private fun DocumentSnapshot.toDraft(): ProblemDraft = ProblemDraft(
        problemId = getString(PROBLEM_ID).orEmpty().ifBlank { id },
        code = getString(CODE).orEmpty(),
        updatedAtMillis = getTimestamp(UPDATED_AT)?.toDate()?.time ?: 0L,
    )

    private fun DocumentSnapshot.toSubmission(problemId: String): ProblemSubmission? {
        val status = runCatching {
            SubmissionStatus.valueOf(getString(STATUS).orEmpty())
        }.getOrNull() ?: return null

        return ProblemSubmission(
            id = id,
            problemId = getString(PROBLEM_ID).orEmpty().ifBlank { problemId },
            status = status,
            passed = getLong(PASSED)?.toInt() ?: 0,
            totalTests = getLong(TOTAL_TESTS)?.toInt() ?: 0,
            executionTimeMs = getLong(EXECUTION_TIME_MS) ?: 0L,
            submittedAtMillis = getTimestamp(SUBMITTED_AT)?.toDate()?.time ?: 0L,
        )
    }

    private fun Throwable.toResourceError(defaultMessage: String) = Resource.Error(
        message = localizedMessage ?: defaultMessage,
        cause = this,
    )

    private companion object {
        const val USERS = "users"
        const val PROBLEM_DRAFTS = "problemDrafts"
        const val PROBLEM_SUBMISSIONS = "problemSubmissions"
        const val SUBMISSION_ITEMS = "items"
        const val PROBLEM_ID = "problemId"
        const val CODE = "code"
        const val STATUS = "status"
        const val PASSED = "passed"
        const val TOTAL_TESTS = "totalTests"
        const val EXECUTION_TIME_MS = "executionTimeMs"
        const val UPDATED_AT = "updatedAt"
        const val SUBMITTED_AT = "submittedAt"
    }
}
