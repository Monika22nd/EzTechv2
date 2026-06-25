package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.data.source.local.ProblemDataSource
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.TestCase
import com.eztech.core.domain.repository.ProblemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * Repository implementation for Python problems and test cases.
 *
 * It tries Firestore first for real demo data, then falls back to bundled seed data when offline or
 * when remote data times out. Results are cached in memory so returning to Problems does not trigger
 * repeated Firestore reads during one app session.
 */
internal class ProblemRepositoryImpl(
    private val remoteDataSource: ProblemDataSource,
    private val localDataSource: ProblemDataSource,
) : ProblemRepository {
    private var cachedProblems: List<Problem>? = null
    private val cachedTestCases = mutableMapOf<String, List<TestCase>>()

    internal constructor(localDataSource: ProblemDataSource) : this(
        remoteDataSource = localDataSource,
        localDataSource = localDataSource,
    )

    /**
     * Emits the full problem list filtered by difficulty.
     *
     * The list is loaded once through getCachedProblems(), so all screens share the same remote/local
     * fallback behavior and the manual EzTech test problem can be merged consistently.
     */
    override fun observeProblems(
        difficulty: Difficulty?,
    ): Flow<Resource<List<Problem>>> = flow<Resource<List<Problem>>> {
        emit(Resource.Loading)
        emit(
            Resource.Success(
                getCachedProblems()
                    .filter { problem -> difficulty == null || problem.difficulty == difficulty }
                    .sortedBy(Problem::order),
            ),
        )
    }.catch { error ->
        emit(error.toResourceError())
    }

    /** Loads one problem from cache when possible, otherwise through remote-first lookup. */
    override suspend fun getProblemById(problemId: String): Resource<Problem> =
        resourceCall {
            cachedProblems
                ?.firstOrNull { problem -> problem.id == problemId }
                ?: remoteFirst { dataSource -> dataSource.getProblem(problemId) }
        }

    /** Loads visible/hidden test cases and caches them per problem for repeated submits. */
    override suspend fun getTestCases(problemId: String): Resource<List<TestCase>> =
        resourceCall {
            cachedTestCases[problemId]
                ?: remoteFirst { dataSource -> dataSource.getTestCases(problemId) }
                    .also { testCases -> cachedTestCases[problemId] = testCases }
        }

    /** Returns the cached list or creates it from remote/local data. */
    private suspend fun getCachedProblems(): List<Problem> =
        cachedProblems ?: remoteFirst { dataSource -> dataSource.getProblems(null) }
            .withLocalTestProblems()
            .also { problems -> cachedProblems = problems }

    /**
     * Adds local-only demo problems when Firestore does not contain them yet.
     *
     * This keeps the test problem available in APK installs without requiring a Firestore import
     * every time a new demo-only problem is added.
     */
    private suspend fun List<Problem>.withLocalTestProblems(): List<Problem> {
        val existingIds = mapTo(mutableSetOf(), Problem::id)
        val localTestProblems = runCatching {
            localDataSource.getProblems(null)
                .filter { problem ->
                    problem.id.startsWith(LOCAL_TEST_PROBLEM_PREFIX) &&
                        problem.id !in existingIds
                }
        }.getOrDefault(emptyList())

        return if (localTestProblems.isEmpty()) {
            this
        } else {
            (this + localTestProblems).sortedBy(Problem::order)
        }
    }

    /** Executes a data-source call against Firestore first and falls back to local seed data. */
    private suspend fun <T> remoteFirst(block: suspend (ProblemDataSource) -> T): T =
        try {
            withTimeout(REMOTE_TIMEOUT_MS) { block(remoteDataSource) }
        } catch (_: TimeoutCancellationException) {
            block(localDataSource)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            block(localDataSource)
        }

    /** Wraps repository exceptions into the shared Resource error type consumed by ViewModels. */
    private suspend fun <T> resourceCall(block: suspend () -> T): Resource<T> =
        runCatching { block() }.fold(
            onSuccess = { value -> Resource.Success(value) },
            onFailure = { error -> error.toResourceError() },
        )

    /** Preserves the original cause while exposing a user-readable message to the UI layer. */
    private fun Throwable.toResourceError() = Resource.Error(
        message = localizedMessage ?: "Unable to load problem data.",
        cause = this,
    )

    private companion object {
        const val REMOTE_TIMEOUT_MS = 8_000L
        const val LOCAL_TEST_PROBLEM_PREFIX = "eztech_test_"
    }
}
