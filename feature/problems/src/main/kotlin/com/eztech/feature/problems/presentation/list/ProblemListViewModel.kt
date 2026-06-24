package com.eztech.feature.problems.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.usecase.problem.GetProblemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProblemListViewModel @Inject constructor(
    private val getProblems: GetProblemsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProblemListUiState())
    val uiState: StateFlow<ProblemListUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadProblems()
    }

    fun selectDifficulty(difficulty: Difficulty?) {
        if (_uiState.value.selectedDifficulty == difficulty) return
        _uiState.update { state -> state.copy(selectedDifficulty = difficulty) }
        loadProblems()
    }

    fun retry() = loadProblems()

    private fun loadProblems() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getProblems(_uiState.value.selectedDifficulty).collect { result ->
                _uiState.update { state ->
                    when (result) {
                        Resource.Loading -> state.copy(isLoading = true, errorMessage = null)
                        is Resource.Success -> state.copy(
                            problems = result.data,
                            isLoading = false,
                            errorMessage = null,
                        )
                        is Resource.Error -> state.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }
}
