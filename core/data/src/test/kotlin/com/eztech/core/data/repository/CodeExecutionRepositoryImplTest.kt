package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.data.engine.PythonEngine
import com.eztech.core.domain.model.CodeExecutionResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeExecutionRepositoryImplTest {

    @Test
    fun `executeCode delegates code and stdin to engine`() = runBlocking {
        val expected = CodeExecutionResult(
            stdout = "Hello Ada\n",
            stderr = "",
            exitCode = 0,
            executionTimeMs = 4,
        )
        val engine = FakePythonEngine(result = expected)
        val repository = CodeExecutionRepositoryImpl(engine)

        val result = repository.executeCode(
            code = "print(input())",
            stdin = "Hello Ada",
        )

        assertEquals("print(input())", engine.receivedCode)
        assertEquals("Hello Ada", engine.receivedStdin)
        assertEquals(expected, (result as Resource.Success).data)
    }

    @Test
    fun `executeCode returns Resource Error when engine fails`() = runBlocking {
        val failure = IllegalStateException("Python failed to start")
        val repository = CodeExecutionRepositoryImpl(
            FakePythonEngine(failure = failure),
        )

        val result = repository.executeCode("print('hello')")

        assertTrue(result is Resource.Error)
        result as Resource.Error
        assertEquals("Python failed to start", result.message)
        assertSame(failure, result.cause)
    }

    private class FakePythonEngine(
        private val result: CodeExecutionResult? = null,
        private val failure: Exception? = null,
    ) : PythonEngine {
        var receivedCode: String? = null
        var receivedStdin: String? = null

        override suspend fun run(
            code: String,
            stdin: String,
        ): CodeExecutionResult {
            receivedCode = code
            receivedStdin = stdin
            failure?.let { throw it }
            return requireNotNull(result)
        }
    }
}
