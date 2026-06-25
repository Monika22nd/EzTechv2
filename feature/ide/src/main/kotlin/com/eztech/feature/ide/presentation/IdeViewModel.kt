package com.eztech.feature.ide.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.usecase.ExecuteCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the standalone Python IDE.
 *
 * It stores editor/stdin/console state and delegates execution to ExecuteCodeUseCase, which is
 * backed by the Chaquopy Python runtime in the data layer.
 */
@HiltViewModel
class IdeViewModel @Inject constructor(
    private val executeCode: ExecuteCodeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IdeUiState())
    val uiState: StateFlow<IdeUiState> = _uiState.asStateFlow()

    /** Updates the editor content as the user types or imports a file. */
    fun onCodeChanged(code: String) {
        _uiState.update { it.copy(code = code) }
    }

    /** Updates stdin used by the next Run action. */
    fun onStdinChanged(stdin: String) {
        _uiState.update { it.copy(stdin = stdin) }
    }

    /** Executes the current code and writes stdout/stderr/timing back to UI state. */
    fun runCode() {
        val request = _uiState.value
        if (request.isRunning) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    stdout = "",
                    stderr = "",
                    exitCode = null,
                    executionTimeMs = null,
                    isRunning = true,
                )
            }

            when (val result = executeCode(request.code, request.stdin)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        stdout = result.data.stdout,
                        stderr = result.data.stderr,
                        exitCode = result.data.exitCode,
                        executionTimeMs = result.data.executionTimeMs,
                        isRunning = false,
                    )
                }

                is Resource.Error -> _uiState.update {
                    it.copy(
                        stderr = result.message,
                        exitCode = 1,
                        isRunning = false,
                    )
                }

                Resource.Loading -> Unit
            }
        }
    }

    /** Clears both code and console output. */
    fun clearEditor() {
        _uiState.update {
            it.copy(
                code = "",
                stdout = "",
                stderr = "",
                exitCode = null,
                executionTimeMs = null,
            )
        }
    }

    /** Clears only console output while leaving editor code intact. */
    fun clearConsole() {
        _uiState.update {
            it.copy(
                stdout = "",
                stderr = "",
                exitCode = null,
                executionTimeMs = null,
            )
        }
    }
}
