package com.eztech.feature.problems.presentation.solve

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.domain.model.SubmissionStatus
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.ide.presentation.component.CodeEditorComposable
import com.eztech.feature.ide.presentation.component.rememberCodeEditorController
import com.eztech.feature.problems.presentation.component.DifficultyBadge
import com.eztech.feature.problems.presentation.component.TestResultCard
import com.eztech.feature.problems.presentation.component.VisibleTestCaseCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemSolveScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProblemSolveViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val editorController = rememberCodeEditorController()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message -> snackbarHostState.showSnackbar(message) }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.problem?.title ?: "Solve problem",
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            state.problem == null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                EzTechEmptyState(
                    title = "Problem unavailable",
                    message = state.errorMessage.orEmpty(),
                    modifier = Modifier.padding(EzTechDimens.ScreenPadding),
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
                    DifficultyBadge(state.problem!!.difficulty)
                    Text(
                        text = "${state.visibleTestCases.size} examples",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 280.dp),
                )
            }
        }
    }
}

@Composable
private fun ResultPanel(
    state: ProblemSolveUiState,
    modifier: Modifier = Modifier,
) {
    val submission = state.submissionResult
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
    ) {
        item {
            if (submission == null) {
                Text(
                    text = "Examples",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = EzTechDimens.SpaceXSmall),
                )
            } else {
                val accepted = submission.status == SubmissionStatus.ACCEPTED
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
                    modifier = Modifier.padding(top = EzTechDimens.SpaceXSmall),
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
        if (submission == null) {
            items(
                items = state.visibleTestCases,
                key = { testCase -> testCase.id },
            ) { testCase ->
                VisibleTestCaseCard(
                    testCase = testCase,
                    index = state.visibleTestCases.indexOf(testCase),
                )
            }
        } else {
            items(
                items = submission.testResults,
                key = { result -> result.testCaseId },
            ) { result ->
                TestResultCard(result)
            }
        }
    }
}

private fun SubmissionStatus.displayName(): String = when (this) {
    SubmissionStatus.ACCEPTED -> "Accepted"
    SubmissionStatus.WRONG_ANSWER -> "Wrong answer"
    SubmissionStatus.RUNTIME_ERROR -> "Runtime error"
    SubmissionStatus.TIME_LIMIT_EXCEEDED -> "Time limit exceeded"
}
