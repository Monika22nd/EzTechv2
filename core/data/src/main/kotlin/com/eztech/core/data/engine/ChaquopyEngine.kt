package com.eztech.core.data.engine

import android.content.Context
import android.os.SystemClock
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.eztech.core.domain.model.CodeExecutionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ChaquopyEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : PythonEngine {

    private val executionMutex = Mutex()

    override suspend fun run(
        code: String,
        stdin: String,
    ): CodeExecutionResult = withContext(Dispatchers.Default) {
        executionMutex.withLock {
            val startedAt = SystemClock.elapsedRealtime()
            ensurePythonStarted()

            val values = Python.getInstance()
                .getModule(RUNNER_MODULE)
                .callAttr(
                    RUN_FUNCTION,
                    code,
                    stdin,
                    EXECUTION_TIMEOUT_SECONDS,
                )
                .asList()

            check(values.size == RESULT_VALUE_COUNT) {
                "Python runner returned an invalid result."
            }

            CodeExecutionResult(
                stdout = values[STDOUT_INDEX].toString(),
                stderr = values[STDERR_INDEX].toString(),
                exitCode = values[EXIT_CODE_INDEX].toInt(),
                executionTimeMs = SystemClock.elapsedRealtime() - startedAt,
            )
        }
    }

    private fun ensurePythonStarted() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    private companion object {
        const val RUNNER_MODULE = "eztech_runner"
        const val RUN_FUNCTION = "run_code"
        const val EXECUTION_TIMEOUT_SECONDS = 10.0
        const val RESULT_VALUE_COUNT = 3
        const val STDOUT_INDEX = 0
        const val STDERR_INDEX = 1
        const val EXIT_CODE_INDEX = 2
    }
}
