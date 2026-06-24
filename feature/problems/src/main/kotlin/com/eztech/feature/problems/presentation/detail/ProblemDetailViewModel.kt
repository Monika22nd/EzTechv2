package com.eztech.feature.problems.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.usecase.problem.GetProblemDetailUseCase
import com.eztech.core.domain.usecase.problem.GetVisibleTestCasesUseCase
import com.eztech.feature.problems.navigation.ProblemsRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProblemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProblem: GetProblemDetailUseCase,
    private val getVisibleTestCases: GetVisibleTestCasesUseCase,
) : ViewModel() {
    private val problemId = savedStateHandle.get<String>(ProblemsRoutes.ProblemIdArg).orEmpty()
    private val _uiState = MutableStateFlow(ProblemDetailUiState())
    val uiState: StateFlow<ProblemDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val problemResult = getProblem(problemId)
            if (problemResult is Resource.Error) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = problemResult.message)
                }
                return@launch
            }

            val testsResult = getVisibleTestCases(problemId)
            _uiState.update { state ->
                when {
                    problemResult is Resource.Success && testsResult is Resource.Success ->
                        state.copy(
                            problem = problemResult.data,
                            visibleTestCases = testsResult.data,
                            isLoading = false,
                        )
                    testsResult is Resource.Error -> state.copy(
                        isLoading = false,
                        errorMessage = testsResult.message,
                    )
                    else -> state.copy(
                        isLoading = false,
                        errorMessage = "Problem data is still loading.",
                    )
                }
            }
        }
    }
}
