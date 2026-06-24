package com.eztech.core.domain.usecase

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.CodeExecutionResult
import com.eztech.core.domain.repository.CodeExecutionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ExecuteCodeUseCaseTest {

    @Test
    fun `blank code returns error without calling repository`() = runBlocking {
        val repository = FakeCodeExecutionRepository()
        val useCase = ExecuteCodeUseCase(repository)

        val result = useCase(code = "   \n")

        assertTrue(result is Resource.Error)
        assertFalse(repository.wasCalled)
    }

    @Test
    fun `valid code delegates unchanged code and stdin`() = runBlocking {
        val expectedResult = Resource.Success(
            CodeExecutionResult(
                stdout = "0\n1\n",
                stderr = "",
                exitCode = 0,
                executionTimeMs = 12,
            ),
        )
        val repository = FakeCodeExecutionRepository(expectedResult)
        val useCase = ExecuteCodeUseCase(repository)
        val code = "for number in range(2):\n    print(number)"
        val stdin = "sample input"

        val result = useCase(code = code, stdin = stdin)

        assertSame(expectedResult, result)
        assertTrue(repository.wasCalled)
        assertEquals(code, repository.receivedCode)
        assertEquals(stdin, repository.receivedStdin)
    }

    private class FakeCodeExecutionRepository(
        private val result: Resource<CodeExecutionResult> = Resource.Error("Not configured"),
    ) : CodeExecutionRepository {
        var wasCalled = false
            private set
        var receivedCode: String? = null
            private set
        var receivedStdin: String? = null
            private set

        override suspend fun executeCode(
            code: String,
            stdin: String,
        ): Resource<CodeExecutionResult> {
            wasCalled = true
            receivedCode = code
            receivedStdin = stdin
            return result
        }
    }
}
