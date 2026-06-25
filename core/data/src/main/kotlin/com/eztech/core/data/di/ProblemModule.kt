package com.eztech.core.data.di

import com.eztech.core.data.repository.ProblemRepositoryImpl
import com.eztech.core.data.repository.ProblemWorkspaceRepositoryImpl
import com.eztech.core.data.source.local.LocalProblemDataSource
import com.eztech.core.data.source.remote.FirebaseProblemDataSource
import com.eztech.core.domain.repository.CodeExecutionRepository
import com.eztech.core.domain.repository.ProblemRepository
import com.eztech.core.domain.repository.ProblemWorkspaceRepository
import com.eztech.core.domain.usecase.problem.GetCodeDraftUseCase
import com.eztech.core.domain.usecase.problem.GetProblemDetailUseCase
import com.eztech.core.domain.usecase.problem.GetProblemSubmissionHistoryUseCase
import com.eztech.core.domain.usecase.problem.GetProblemsUseCase
import com.eztech.core.domain.usecase.problem.GetVisibleTestCasesUseCase
import com.eztech.core.domain.usecase.problem.RecordProblemSubmissionUseCase
import com.eztech.core.domain.usecase.problem.RunCustomInputUseCase
import com.eztech.core.domain.usecase.problem.SaveCodeDraftUseCase
import com.eztech.core.domain.usecase.problem.SubmitSolutionUseCase
import com.google.firebase.firestore.FirebaseFirestore
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
    @Singleton
    internal fun provideProblemWorkspaceRepository(
        firestore: FirebaseFirestore,
    ): ProblemWorkspaceRepository = ProblemWorkspaceRepositoryImpl(firestore)

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

    @Provides
    fun provideRunCustomInputUseCase(
        codeExecutionRepository: CodeExecutionRepository,
    ) = RunCustomInputUseCase(codeExecutionRepository)

    @Provides
    fun provideGetCodeDraftUseCase(
        repository: ProblemWorkspaceRepository,
    ) = GetCodeDraftUseCase(repository)

    @Provides
    fun provideSaveCodeDraftUseCase(
        repository: ProblemWorkspaceRepository,
    ) = SaveCodeDraftUseCase(repository)

    @Provides
    fun provideRecordProblemSubmissionUseCase(
        repository: ProblemWorkspaceRepository,
    ) = RecordProblemSubmissionUseCase(repository)

    @Provides
    fun provideGetProblemSubmissionHistoryUseCase(
        repository: ProblemWorkspaceRepository,
    ) = GetProblemSubmissionHistoryUseCase(repository)
}
