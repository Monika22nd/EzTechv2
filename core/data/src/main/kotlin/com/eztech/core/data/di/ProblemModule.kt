package com.eztech.core.data.di

import com.eztech.core.data.repository.ProblemRepositoryImpl
import com.eztech.core.data.source.local.LocalProblemDataSource
import com.eztech.core.data.source.remote.FirebaseProblemDataSource
import com.eztech.core.domain.repository.CodeExecutionRepository
import com.eztech.core.domain.repository.ProblemRepository
import com.eztech.core.domain.usecase.problem.GetProblemDetailUseCase
import com.eztech.core.domain.usecase.problem.GetProblemsUseCase
import com.eztech.core.domain.usecase.problem.GetVisibleTestCasesUseCase
import com.eztech.core.domain.usecase.problem.SubmitSolutionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProblemModule {
    @Provides
    @Singleton
    internal fun provideProblemRepository(
        remoteDataSource: FirebaseProblemDataSource,
        localDataSource: LocalProblemDataSource,
    ): ProblemRepository = ProblemRepositoryImpl(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
    )

    @Provides
    fun provideGetProblemsUseCase(
        problemRepository: ProblemRepository,
    ) = GetProblemsUseCase(problemRepository)

    @Provides
    fun provideGetProblemDetailUseCase(
        problemRepository: ProblemRepository,
    ) = GetProblemDetailUseCase(problemRepository)

    @Provides
    fun provideGetVisibleTestCasesUseCase(
        problemRepository: ProblemRepository,
    ) = GetVisibleTestCasesUseCase(problemRepository)

    @Provides
    fun provideSubmitSolutionUseCase(
        problemRepository: ProblemRepository,
        codeExecutionRepository: CodeExecutionRepository,
    ) = SubmitSolutionUseCase(
        problemRepository = problemRepository,
        codeExecutionRepository = codeExecutionRepository,
    )
}
