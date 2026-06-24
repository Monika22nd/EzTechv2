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
    internal constructor(localDataSource: ProblemDataSource) : this(
        remoteDataSource = localDataSource,
        localDataSource = localDataSource,
    )

    override fun observeProblems(
        difficulty: Difficulty?,
    ): Flow<Resource<List<Problem>>> = flow<Resource<List<Problem>>> {
        emit(Resource.Loading)
        emit(Resource.Success(remoteFirst { dataSource -> dataSource.getProblems(difficulty) }))
    }.catch { error ->
        emit(error.toResourceError())
    }

    override suspend fun getProblemById(problemId: String): Resource<Problem> =
        resourceCall { remoteFirst { dataSource -> dataSource.getProblem(problemId) } }

    override suspend fun getTestCases(problemId: String): Resource<List<TestCase>> =
        resourceCall { remoteFirst { dataSource -> dataSource.getTestCases(problemId) } }

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
