package com.eztech.feature.problems.presentation.solve

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.usecase.problem.GetProblemDetailUseCase
import com.eztech.core.domain.usecase.problem.GetVisibleTestCasesUseCase
import com.eztech.core.domain.usecase.problem.SubmitSolutionUseCase
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.usecase.gamification.CompleteProblemUseCase
import com.eztech.feature.problems.navigation.ProblemsRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class ProblemSolveViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProblem: GetProblemDetailUseCase,
    private val getVisibleTestCases: GetVisibleTestCasesUseCase,
    private val submitSolution: SubmitSolutionUseCase,
    private val authRepository: AuthRepository,
    private val completeProblem: CompleteProblemUseCase,
) : ViewModel() {
    private val problemId = savedStateHandle.get<String>(ProblemsRoutes.ProblemIdArg).orEmpty()
    private val _uiState = MutableStateFlow(ProblemSolveUiState())
    val uiState: StateFlow<ProblemSolveUiState> = _uiState.asStateFlow()
    private val solveStartedAtNanos = System.nanoTime()

    init {
        load()
    }

    fun onCodeChanged(code: String) {
        _uiState.update {
            it.copy(
                code = code,
                submissionResult = null,
                completion = null,
                errorMessage = null,
            )
        }
    }

    fun resetCode() {
        val starterCode = _uiState.value.problem?.starterCode ?: return
        _uiState.update {
            it.copy(
                code = starterCode,
                submissionResult = null,
                completion = null,
                errorMessage = null,
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    submissionResult = null,
                    completion = null,
                    errorMessage = null,
                )
            }
            when (val result = submitSolution(problemId, state.code)) {
                is Resource.Success -> _uiState.update {
                    it.copy(submissionResult = result.data)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }

            val submission = _uiState.value.submissionResult
            if (submission?.accepted == true) {
                saveAcceptedProblem()
            } else {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private suspend fun saveAcceptedProblem() {
        val problem = _uiState.value.problem
        val user = authRepository.observeCurrentUser().first()
        if (problem == null || user == null) {
            _uiState.update { it.copy(isSubmitting = false) }
            return
        }
        val durationSeconds = ((System.nanoTime() - solveStartedAtNanos) / 1_000_000_000L)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        when (
            val result = completeProblem(
                userId = user.uid,
                problemId = problem.id,
                difficulty = problem.difficulty,
                solveDurationSeconds = durationSeconds,
            )
        ) {
            is Resource.Success -> _uiState.update {
                it.copy(completion = result.data, isSubmitting = false)
            }
            is Resource.Error -> _uiState.update {
                it.copy(
                    isSubmitting = false,
                    errorMessage = "Accepted, but progress was not saved: ${result.message}",
                )
            }
            Resource.Loading -> _uiState.update { it.copy(isSubmitting = false) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val problemResult = getProblem(problemId)
            val testsResult = getVisibleTestCases(problemId)
            _uiState.update { state ->
                when {
                    problemResult is Resource.Success && testsResult is Resource.Success ->
                        state.copy(
                            problem = problemResult.data,
                            visibleTestCases = testsResult.data,
                            code = problemResult.data.starterCode,
                            isLoading = false,
                        )
                    problemResult is Resource.Error -> state.copy(
                        isLoading = false,
                        errorMessage = problemResult.message,
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
