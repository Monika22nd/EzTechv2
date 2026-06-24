package com.eztech.core.data.di

import com.eztech.core.data.engine.ChaquopyEngine
import com.eztech.core.data.engine.PythonEngine
import com.eztech.core.data.repository.CodeExecutionRepositoryImpl
import com.eztech.core.domain.repository.CodeExecutionRepository
import com.eztech.core.domain.usecase.ExecuteCodeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CodeExecutionModule {

    @Provides
    @Singleton
    internal fun providePythonEngine(
        engine: ChaquopyEngine,
    ): PythonEngine = engine

    @Provides
    @Singleton
    internal fun provideCodeExecutionRepository(
        pythonEngine: PythonEngine,
    ): CodeExecutionRepository = CodeExecutionRepositoryImpl(pythonEngine)

    @Provides
    fun provideExecuteCodeUseCase(
        repository: CodeExecutionRepository,
    ) = ExecuteCodeUseCase(repository)
}
