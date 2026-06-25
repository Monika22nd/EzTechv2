package com.eztech.feature.problems.presentation.solve

import com.eztech.core.domain.model.CodeExecutionResult
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.ProblemCompletion
import com.eztech.core.domain.model.ProblemSubmission
import com.eztech.core.domain.model.SubmissionResult
import com.eztech.core.domain.model.TestCase

data class ProblemSolveUiState(
    val problem: Problem? = null,
    val visibleTestCases: List<TestCase> = emptyList(),
    val code: String = "",
    val customInput: String = "",
    val customRunResult: CodeExecutionResult? = null,
    val customRunErrorMessage: String? = null,
    val submissionResult: SubmissionResult? = null,
    val submissionHistory: List<ProblemSubmission> = emptyList(),
    val completion: ProblemCompletion? = null,
    val showCompletionDialog: Boolean = false,
    val selectedPanelTab: SolvePanelTab = SolvePanelTab.EXAMPLES,
    val draftStatus: DraftStatus = DraftStatus.NONE,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val isRunningCustomInput: Boolean = false,
    val historyErrorMessage: String? = null,
    val errorMessage: String? = null,
)

enum class SolvePanelTab(val label: String) {
    EXAMPLES("Examples"),
    CUSTOM_INPUT("Input"),
    RESULTS("Results"),
    HISTORY("History"),
}

enum class DraftStatus {
    NONE,
    SAVING,
    SAVED,
    ERROR,
}
