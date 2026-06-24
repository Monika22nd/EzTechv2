package com.eztech.feature.ide.presentation

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.CodeExecutionResult
import com.eztech.core.domain.repository.CodeExecutionRepository
import com.eztech.core.domain.usecase.ExecuteCodeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IdeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `runCode publishes successful execution`() = runTest(dispatcher) {
        val repository = FakeCodeExecutionRepository(
            Resource.Success(
                CodeExecutionResult(
                    stdout = "Hello\n",
                    stderr = "",
                    exitCode = 0,
                    executionTimeMs = 12,
                ),
            ),
        )
        val viewModel = IdeViewModel(ExecuteCodeUseCase(repository))
        viewModel.onCodeChanged("print('Hello')")

        viewModel.runCode()
        advanceUntilIdle()

        assertEquals("Hello\n", viewModel.uiState.value.stdout)
        assertEquals(0, viewModel.uiState.value.exitCode)
        assertEquals(12L, viewModel.uiState.value.executionTimeMs)
        assertFalse(viewModel.uiState.value.isRunning)
    }

    @Test
    fun `runCode publishes repository error`() = runTest(dispatcher) {
        val viewModel = IdeViewModel(
            ExecuteCodeUseCase(
                FakeCodeExecutionRepository(Resource.Error("Python unavailable")),
            ),
        )
        viewModel.onCodeChanged("print('Hello')")

        viewModel.runCode()
        advanceUntilIdle()

        assertEquals("Python unavailable", viewModel.uiState.value.stderr)
        assertEquals(1, viewModel.uiState.value.exitCode)
        assertFalse(viewModel.uiState.value.isRunning)
    }

    private class FakeCodeExecutionRepository(
        private val result: Resource<CodeExecutionResult>,
    ) : CodeExecutionRepository {
        override suspend fun executeCode(
            code: String,
            stdin: String,
        ): Resource<CodeExecutionResult> = result
    }
}
