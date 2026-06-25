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

    override suspend fun getProblemById(problemId: String): Resource<Problem> =
        resourceCall {
            cachedProblems
                ?.firstOrNull { problem -> problem.id == problemId }
                ?: remoteFirst { dataSource -> dataSource.getProblem(problemId) }
        }

    override suspend fun getTestCases(problemId: String): Resource<List<TestCase>> =
        resourceCall {
            cachedTestCases[problemId]
                ?: remoteFirst { dataSource -> dataSource.getTestCases(problemId) }
                    .also { testCases -> cachedTestCases[problemId] = testCases }
        }

    private suspend fun getCachedProblems(): List<Problem> =
        cachedProblems ?: remoteFirst { dataSource -> dataSource.getProblems(null) }
            .also { problems -> cachedProblems = problems }

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

    private suspend fun <T> resourceCall(block: suspend () -> T): Resource<T> =
        runCatching { block() }.fold(
            onSuccess = { value -> Resource.Success(value) },
            onFailure = { error -> error.toResourceError() },
        )

    private fun Throwable.toResourceError() = Resource.Error(
        message = localizedMessage ?: "Unable to load problem data.",
        cause = this,
    )

    private companion object {
        const val REMOTE_TIMEOUT_MS = 8_000L
    }
}
