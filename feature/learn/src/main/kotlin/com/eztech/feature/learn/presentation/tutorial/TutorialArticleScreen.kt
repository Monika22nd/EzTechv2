package com.eztech.feature.learn.presentation.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.eztech.core.ui.component.EzTechButton
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.learn.presentation.component.BookmarkButton
import com.eztech.feature.learn.presentation.component.LearnTopBar

@Composable
fun TutorialArticleScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TutorialArticleViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            LearnTopBar(
                title = "Tutorial",
                onBackClick = onBackClick,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            state.errorMessage != null -> EzTechEmptyState(
                title = "Could not open this tutorial",
                message = state.errorMessage.orEmpty(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(EzTechDimens.ScreenPadding),
            )

            state.lesson != null -> {
                val lesson = state.lesson ?: return@Scaffold
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = EzTechDimens.ScreenPadding,
                        top = EzTechDimens.SpaceLarge,
                        end = EzTechDimens.ScreenPadding,
                        bottom = 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceLarge),
                ) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = lesson.title,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (lesson.watched) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Completed",
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.tertiary,
                                    )
                                }
                                BookmarkButton(
                                    bookmarked = lesson.bookmarked,
                                    onClick = viewModel::toggleBookmark,
                                )
                            }
                            Text(
                                text = lesson.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = lesson.sourceName
                                    .ifBlank { "PyQuest Tutorial" }
                                    .replace("EzTech", "PyQuest"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    item { HorizontalDivider() }
                    item {
                        Text(
                            text = lesson.content
                                .ifBlank { "Tutorial content is being prepared." }
                                .replace("EzTech", "PyQuest"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    item {
                        EzTechButton(
                            text = if (lesson.watched) "Completed" else "Mark complete",
                            onClick = viewModel::markComplete,
                            enabled = !lesson.watched,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
