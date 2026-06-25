package com.eztech.feature.problems.presentation.solve

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.model.SubmissionResult
import com.eztech.core.domain.usecase.problem.GetProblemDetailUseCase
import com.eztech.core.domain.usecase.problem.GetVisibleTestCasesUseCase
import com.eztech.core.domain.usecase.problem.GetCodeDraftUseCase
import com.eztech.core.domain.usecase.problem.GetProblemSubmissionHistoryUseCase
import com.eztech.core.domain.usecase.problem.RecordProblemSubmissionUseCase
import com.eztech.core.domain.usecase.problem.RunCustomInputUseCase
import com.eztech.core.domain.usecase.problem.SaveCodeDraftUseCase
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ViewModel for solving one problem.
 *
 * It coordinates loading problem data, autosaving code drafts, running custom input, submitting test
 * cases, recording submission history, and awarding progress when a solution is accepted.
 */
@HiltViewModel
class ProblemSolveViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProblem: GetProblemDetailUseCase,
    private val getVisibleTestCases: GetVisibleTestCasesUseCase,
    private val submitSolution: SubmitSolutionUseCase,
    private val runCustomInputUseCase: RunCustomInputUseCase,
    private val authRepository: AuthRepository,
    private val completeProblem: CompleteProblemUseCase,
    private val getCodeDraft: GetCodeDraftUseCase,
    private val saveCodeDraft: SaveCodeDraftUseCase,
    private val recordProblemSubmission: RecordProblemSubmissionUseCase,
    private val getSubmissionHistory: GetProblemSubmissionHistoryUseCase,
) : ViewModel() {
    private val problemId = savedStateHandle.get<String>(ProblemsRoutes.ProblemIdArg).orEmpty()
    private val _uiState = MutableStateFlow(ProblemSolveUiState())
    val uiState: StateFlow<ProblemSolveUiState> = _uiState.asStateFlow()
    private val solveStartedAtNanos = System.nanoTime()
    private var currentUserId: String? = null
    private var saveDraftJob: Job? = null
    private var historyJob: Job? = null

    init {
        load()
    }

    /** Updates editor code and schedules an autosave draft after a short debounce. */
    fun onCodeChanged(code: String) {
        _uiState.update {
            it.copy(
                code = code,
                customRunResult = null,
                customRunErrorMessage = null,
                submissionResult = null,
                completion = null,
                showCompletionDialog = false,
                errorMessage = null,
                draftStatus = DraftStatus.SAVING,
            )
        }
        scheduleDraftSave(code)
    }

    /** Updates stdin text for the Custom Input tab. */
    fun onCustomInputChanged(input: String) {
        _uiState.update {
            it.copy(
                customInput = input,
                customRunResult = null,
                customRunErrorMessage = null,
            )
        }
    }

    /** Switches between examples, custom input, results, and history panels. */
    fun selectPanelTab(tab: SolvePanelTab) {
        _uiState.update { it.copy(selectedPanelTab = tab) }
    }

    /** Reloads problem details and visible test cases after a load error. */
    fun retry() = load()

    /** Hides the accepted-solution dialog after the learner closes it. */
    fun dismissCompletionDialog() {
        _uiState.update { it.copy(showCompletionDialog = false) }
    }

    /** Restores starter code and immediately saves it as the active draft. */
    fun resetCode() {
        val starterCode = _uiState.value.problem?.starterCode ?: return
        _uiState.update {
            it.copy(
                code = starterCode,
                customRunResult = null,
                customRunErrorMessage = null,
                submissionResult = null,
                completion = null,
                showCompletionDialog = false,
                errorMessage = null,
                draftStatus = DraftStatus.SAVING,
            )
        }
        scheduleDraftSave(starterCode, delayMillis = 0L)
    }

    /**
     * Runs the current editor code with custom stdin.
     *
     * This does not grade the problem; it is a practice console so users can quickly inspect stdout
     * and stderr before submitting official tests.
     */
    fun runCustomInput() {
        val state = _uiState.value
        if (state.isRunningCustomInput) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRunningCustomInput = true,
                    customRunResult = null,
                    customRunErrorMessage = null,
                    selectedPanelTab = SolvePanelTab.CUSTOM_INPUT,
                )
            }
            when (val result = runCustomInputUseCase(state.code, state.customInput)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        customRunResult = result.data,
                        isRunningCustomInput = false,
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(
                        customRunErrorMessage = result.message,
                        isRunningCustomInput = false,
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    /**
     * Runs all official test cases and records the attempt.
     *
     * Accepted submissions trigger gamification progress; failed submissions still appear in history
     * so the user can review attempts.
     */
    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    submissionResult = null,
                    completion = null,
                    showCompletionDialog = false,
                    errorMessage = null,
                    selectedPanelTab = SolvePanelTab.RESULTS,
                )
            }
            saveDraftNow(state.code)
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
            if (submission != null) {
                recordSubmission(submission)
            }
            if (submission?.accepted == true) {
                saveAcceptedProblem()
            } else {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    /** Persists solved-problem progress, EXP, level, streak, and newly unlocked badges. */
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
                it.copy(
                    completion = result.data,
                    showCompletionDialog = true,
                    isSubmitting = false,
                )
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

    /** Loads problem detail, visible examples, current user, draft code, and history stream. */
    private fun load() {
        viewModelScope.launch {
            val user = authRepository.observeCurrentUser().first()
            currentUserId = user?.uid
            observeHistory(user?.uid)

            val problemResult = getProblem(problemId)
            val testsResult = getVisibleTestCases(problemId)
            val draftResult = user?.uid?.let { userId ->
                getCodeDraft(userId = userId, problemId = problemId)
            }
            _uiState.update { state ->
                when {
                    problemResult is Resource.Success && testsResult is Resource.Success ->
                        state.copy(
                            problem = problemResult.data,
                            visibleTestCases = testsResult.data,
                            code = draftResult.savedDraftCode()
                                ?: problemResult.data.starterCode,
                            draftStatus = if (draftResult.savedDraftCode() != null) {
                                DraftStatus.SAVED
                            } else {
                                DraftStatus.NONE
                            },
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

    /** Subscribes to submission history for this problem and user. */
    private fun observeHistory(userId: String?) {
        historyJob?.cancel()
        if (userId == null) return
        historyJob = viewModelScope.launch {
            getSubmissionHistory(
                userId = userId,
                problemId = problemId,
            ).collect { result ->
                _uiState.update { state ->
                    when (result) {
                        Resource.Loading -> state
                        is Resource.Success -> state.copy(
                            submissionHistory = result.data,
                            historyErrorMessage = null,
                        )
                        is Resource.Error -> state.copy(
                            historyErrorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    /** Debounces draft writes to avoid saving on every keystroke. */
    private fun scheduleDraftSave(
        code: String,
        delayMillis: Long = DRAFT_SAVE_DELAY_MS,
    ) {
        saveDraftJob?.cancel()
        saveDraftJob = viewModelScope.launch {
            delay(delayMillis)
            saveDraftNow(code)
        }
    }

    /** Writes the current solution draft to the signed-in user's Firestore draft document. */
    private suspend fun saveDraftNow(code: String) {
        val userId = currentUserId
        if (userId == null) {
            _uiState.update { it.copy(draftStatus = DraftStatus.NONE) }
            return
        }

        when (
            val result = saveCodeDraft(
                userId = userId,
                problemId = problemId,
                code = code,
            )
        ) {
            is Resource.Success -> _uiState.update {
                it.copy(draftStatus = DraftStatus.SAVED)
            }
            is Resource.Error -> _uiState.update {
                it.copy(draftStatus = DraftStatus.ERROR)
            }
            Resource.Loading -> Unit
        }
    }

    /** Stores one submission attempt for the History tab. */
    private suspend fun recordSubmission(submission: SubmissionResult) {
        val userId = currentUserId ?: return
        when (
            val result = recordProblemSubmission(
                userId = userId,
                problemId = problemId,
                result = submission,
            )
        ) {
            is Resource.Success -> Unit
            is Resource.Error -> _uiState.update {
                it.copy(historyErrorMessage = result.message)
            }
            Resource.Loading -> Unit
        }
    }

    /** Extracts non-blank draft code only when the draft resource loaded successfully. */
    private fun Resource<com.eztech.core.domain.model.ProblemDraft?>?.savedDraftCode(): String? =
        (this as? Resource.Success)
            ?.data
            ?.code
            ?.takeIf(String::isNotBlank)

    private companion object {
        const val DRAFT_SAVE_DELAY_MS = 700L
    }
}
