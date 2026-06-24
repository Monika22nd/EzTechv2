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

@HiltViewModel
class IdeViewModel @Inject constructor(
    private val executeCode: ExecuteCodeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IdeUiState())
    val uiState: StateFlow<IdeUiState> = _uiState.asStateFlow()

    fun onCodeChanged(code: String) {
        _uiState.update { it.copy(code = code) }
    }

    fun onStdinChanged(stdin: String) {
        _uiState.update { it.copy(stdin = stdin) }
    }

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
