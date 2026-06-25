package com.eztech.feature.problems.presentation.solve

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.domain.model.CodeExecutionResult
import com.eztech.core.domain.model.ProblemSubmission
import com.eztech.core.domain.model.SubmissionStatus
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.component.EzTechTopBar
import com.eztech.core.ui.file.readUtf8Text
import com.eztech.core.ui.file.writeUtf8Text
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.ide.presentation.component.CodeEditorComposable
import com.eztech.feature.ide.presentation.component.rememberCodeEditorController
import com.eztech.feature.problems.presentation.component.DifficultyBadge
import com.eztech.feature.problems.presentation.component.SubmissionSuccessDialog
import com.eztech.feature.problems.presentation.component.TestResultCard
import com.eztech.feature.problems.presentation.component.VisibleTestCaseCard
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Solve screen for one Python practice problem.
 *
 * The screen combines problem content, a Python code editor, visible examples, custom input,
 * submission results, submission history, and import/export actions for `.py` files.
 */
@Composable
fun ProblemSolveScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProblemSolveViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val editorController = rememberCodeEditorController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    /** Shows non-blocking feedback for import/export and problem errors. */
    fun showFileMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    /** Imports a selected UTF-8 text/Python file into the current solution draft. */
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.readUtf8Text(uri)
            .onSuccess { code ->
                viewModel.onCodeChanged(code)
                editorController.requestFocus()
                showFileMessage("File imported.")
            }
            .onFailure { error ->
                showFileMessage(error.localizedMessage ?: "Unable to import file.")
            }
    }

    /** Exports the current solution code to a user-created `.py` document. */
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.writeUtf8Text(uri, state.code)
            .onSuccess { showFileMessage("File exported.") }
            .onFailure { error ->
                showFileMessage(error.localizedMessage ?: "Unable to export file.")
            }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message -> snackbarHostState.showSnackbar(message) }
    }

    if (state.showCompletionDialog) {
        state.completion?.let { completion ->
            SubmissionSuccessDialog(
                completion = completion,
                onDismiss = viewModel::dismissCompletionDialog,
            )
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EzTechTopBar(
                title = state.problem?.title ?: "Solve problem",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = {
                            importLauncher.launch(arrayOf("text/*", "application/octet-stream"))
                        },
                        enabled = !state.isSubmitting && state.problem != null,
                    ) {
                        Icon(Icons.Rounded.FileOpen, contentDescription = "Import Python file")
                    }
                    IconButton(
                        onClick = {
                            exportLauncher.launch(state.exportFileName())
                        },
                        enabled = !state.isSubmitting && state.problem != null,
                    ) {
                        Icon(Icons.Rounded.SaveAlt, contentDescription = "Export Python file")
                    }
                    IconButton(
                        onClick = viewModel::resetCode,
                        enabled = !state.isSubmitting && state.problem != null,
                    ) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = "Reset code")
                    }
                    Button(
                        onClick = viewModel::submit,
                        enabled = !state.isSubmitting && state.problem != null,
                        modifier = Modifier.padding(end = EzTechDimens.SpaceSmall),
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = EzTechDimens.SpaceSmall),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = null,
                                modifier = Modifier.padding(end = EzTechDimens.SpaceSmall),
                            )
                        }
                        Text("Submit")
                    }
                },
            )
        },
    ) { innerPadding ->
        val problem = state.problem
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            problem == null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                EzTechEmptyState(
                    title = "Problem unavailable",
                    message = state.errorMessage ?: "This problem could not be loaded.",
                    modifier = Modifier.padding(EzTechDimens.ScreenPadding),
                    actionLabel = "Try again",
                    onAction = viewModel::retry,
                )
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = EzTechDimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
                ) {
                    DifficultyBadge(problem.difficulty)
                    Text(
                        text = "${state.visibleTestCases.size} examples",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = state.draftStatus.displayText(),
                        style = MaterialTheme.typography.labelMedium,
                        color = state.draftStatus.textColor(),
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = MaterialTheme.shapes.small,
                        ),
                    shape = MaterialTheme.shapes.small,
                ) {
                    CodeEditorComposable(
                        code = state.code,
                        fontSizeSp = 14f,
                        controller = editorController,
                        onCodeChanged = viewModel::onCodeChanged,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                ResultPanel(
                    state = state,
                    onPanelTabSelected = viewModel::selectPanelTab,
                    onCustomInputChanged = viewModel::onCustomInputChanged,
                    onRunCustomInput = viewModel::runCustomInput,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 170.dp, max = 360.dp),
                )
            }
        }
    }
}

/**
 * Bottom panel under the editor.
 *
 * Tabs separate examples, custom input, submit results, and submission history so the editor can
 * stay visible while the learner checks different feedback modes.
 */
@Composable
private fun ResultPanel(
    state: ProblemSolveUiState,
    onPanelTabSelected: (SolvePanelTab) -> Unit,
    onCustomInputChanged: (String) -> Unit,
    onRunCustomInput: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
    ) {
        item {
            TabRow(selectedTabIndex = state.selectedPanelTab.ordinal) {
                SolvePanelTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedPanelTab == tab,
                        onClick = { onPanelTabSelected(tab) },
                        text = { Text(tab.label) },
                    )
                }
            }
        }

        when (state.selectedPanelTab) {
            SolvePanelTab.EXAMPLES -> {
                items(
                    items = state.visibleTestCases,
                    key = { testCase -> testCase.id },
                ) { testCase ->
                    VisibleTestCaseCard(
                        testCase = testCase,
                        index = state.visibleTestCases.indexOf(testCase),
                    )
                }
            }

            SolvePanelTab.CUSTOM_INPUT -> {
                item {
                    CustomInputPanel(
                        state = state,
                        onCustomInputChanged = onCustomInputChanged,
                        onRunCustomInput = onRunCustomInput,
                    )
                }
            }

            SolvePanelTab.RESULTS -> {
                item { SubmissionSummary(state = state) }
                state.submissionResult?.let { submission ->
                    items(
                        items = submission.testResults,
                        key = { result -> result.testCaseId },
                    ) { result ->
                        TestResultCard(result)
                    }
                }
            }

            SolvePanelTab.HISTORY -> {
                state.historyErrorMessage?.let { message ->
                    item {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                if (state.submissionHistory.isEmpty()) {
                    item {
                        EzTechEmptyState(
                            title = "No submissions yet",
                            message = "Submit your code to build history.",
                        )
                    }
                } else {
                    items(
                        items = state.submissionHistory,
                        key = ProblemSubmission::id,
                    ) { submission ->
                        SubmissionHistoryCard(submission = submission)
                    }
                }
            }
        }
    }
}

/** Displays pass/fail summary after Submit and the EXP/badge result for accepted solutions. */
@Composable
private fun SubmissionSummary(
    state: ProblemSolveUiState,
    modifier: Modifier = Modifier,
) {
    val submission = state.submissionResult
    Column(
        modifier = modifier.padding(top = EzTechDimens.SpaceXSmall),
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall),
    ) {
        if (submission == null) {
            Text(
                text = "No result yet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Submit your code to run all test cases.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        val accepted = submission.status == SubmissionStatus.ACCEPTED
        val progress = if (submission.totalTests == 0) {
            0f
        } else {
            submission.passed.toFloat() / submission.totalTests.toFloat()
        }
        Text(
            text = if (accepted) {
                "Accepted: ${submission.passed}/${submission.totalTests} tests"
            } else {
                "${submission.status.displayName()}: " +
                    "${submission.passed}/${submission.totalTests} tests"
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (accepted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = if (accepted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        )
        Text(
            text = "Total execution: ${submission.executionTimeMs} ms",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        state.completion?.let { completion ->
            Text(
                text = if (completion.firstSolve) {
                    "+${completion.awardedExp} EXP  |  Level ${completion.progress.level}"
                } else {
                    "Previously solved  |  No additional EXP"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            if (completion.newlyUnlockedBadges.isNotEmpty()) {
                Text(
                    text = "Badge unlocked: " + completion.newlyUnlockedBadges
                        .joinToString { badge -> badge.name },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

/** Text input and run button for executing the current code against custom stdin. */
@Composable
private fun CustomInputPanel(
    state: ProblemSolveUiState,
    onCustomInputChanged: (String) -> Unit,
    onRunCustomInput: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(top = EzTechDimens.SpaceXSmall),
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
    ) {
        OutlinedTextField(
            value = state.customInput,
            onValueChange = onCustomInputChanged,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            label = { Text("Custom stdin") },
            placeholder = { Text("Input for your program") },
        )
        Button(
            onClick = onRunCustomInput,
            enabled = !state.isRunningCustomInput,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isRunningCustomInput) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = EzTechDimens.SpaceSmall),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(EzTechDimens.SpaceSmall))
            }
            Text("Run with input")
        }
        state.customRunErrorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        state.customRunResult?.let { result ->
            CustomRunResultCard(result = result)
        }
    }
}

/** Shows stdout/stderr from a custom run without affecting official test results. */
@Composable
private fun CustomRunResultCard(
    result: CodeExecutionResult,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(EzTechDimens.SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
        ) {
            Text(
                text = "Exit ${result.exitCode} | ${result.executionTimeMs} ms",
                style = MaterialTheme.typography.labelLarge,
                color = if (result.isSuccess) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                fontWeight = FontWeight.SemiBold,
            )
            CodeOutput(label = "stdout", value = result.stdout.ifBlank { "<empty>" })
            if (result.stderr.isNotBlank()) {
                CodeOutput(label = "stderr", value = result.stderr)
            }
        }
    }
}

/** Compact row for one previous submission attempt. */
@Composable
private fun SubmissionHistoryCard(
    submission: ProblemSubmission,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (submission.accepted) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
        },
    ) {
        Row(
            modifier = Modifier.padding(EzTechDimens.SpaceMedium),
            horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = submission.status.displayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (submission.accepted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                Text(
                    text = "${submission.passed}/${submission.totalTests} tests",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${submission.executionTimeMs} ms",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = submission.submittedAtMillis.formatTime(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Monospace output block shared by stdout and stderr sections. */
@Composable
private fun CodeOutput(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
        )
    }
}

/** Converts domain submission status enum into short UI text. */
private fun SubmissionStatus.displayName(): String = when (this) {
    SubmissionStatus.ACCEPTED -> "Accepted"
    SubmissionStatus.WRONG_ANSWER -> "Wrong answer"
    SubmissionStatus.RUNTIME_ERROR -> "Runtime error"
    SubmissionStatus.TIME_LIMIT_EXCEEDED -> "Time limit exceeded"
}

/** Chooses the status text color for autosaved code drafts. */
@Composable
private fun DraftStatus.textColor() = when (this) {
    DraftStatus.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    DraftStatus.SAVING -> MaterialTheme.colorScheme.primary
    DraftStatus.SAVED -> MaterialTheme.colorScheme.tertiary
    DraftStatus.ERROR -> MaterialTheme.colorScheme.error
}

/** Converts draft state into the short status text shown under the editor. */
private fun DraftStatus.displayText(): String = when (this) {
    DraftStatus.NONE -> ""
    DraftStatus.SAVING -> "Saving draft"
    DraftStatus.SAVED -> "Draft saved"
    DraftStatus.ERROR -> "Draft not saved"
}

/** Formats submission timestamps for the history tab. */
private fun Long.formatTime(): String {
    if (this <= 0L) return ""
    return SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(this))
}

/** Builds a safe default filename when exporting a solution from the Solve screen. */
private fun ProblemSolveUiState.exportFileName(): String {
    val baseName = problem?.id
        ?.takeIf(String::isNotBlank)
        ?: problem?.title
        ?: "solution"
    val safeName = baseName
        .lowercase(Locale.US)
        .replace(Regex("[^a-z0-9_-]+"), "_")
        .trim('_')
        .ifBlank { "solution" }
    return "$safeName.py"
}
