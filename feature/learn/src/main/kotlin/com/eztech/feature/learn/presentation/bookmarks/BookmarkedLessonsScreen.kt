package com.eztech.feature.learn.presentation.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.domain.model.Lesson
import com.eztech.core.ui.component.EzTechButton
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.learn.presentation.component.LearnTopBar
import com.eztech.feature.learn.presentation.component.LessonCard

@Composable
fun BookmarkedLessonsScreen(
    onBackClick: () -> Unit,
    onLessonClick: (Lesson) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookmarkedLessonsViewModel = hiltViewModel(),
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
                title = "Bookmarks",
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

            state.errorMessage != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(EzTechDimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(
                    space = EzTechDimens.SpaceLarge,
                    alignment = Alignment.CenterVertically,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EzTechEmptyState(
                    title = "Could not load bookmarks",
                    message = state.errorMessage.orEmpty(),
                )
                EzTechButton(text = "Try again", onClick = viewModel::retry)
            }

            state.lessons.isEmpty() -> EzTechEmptyState(
                title = "No bookmarks yet",
                message = "Bookmark lessons to find them here later.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(EzTechDimens.ScreenPadding),
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = EzTechDimens.ScreenPadding,
                    top = EzTechDimens.SpaceMedium,
                    end = EzTechDimens.ScreenPadding,
                    bottom = 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            ) {
                item {
                    Text(
                        text = "${state.lessons.size} saved lessons",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(
                    items = state.lessons,
                    key = Lesson::id,
                ) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        onClick = { onLessonClick(lesson) },
                        onBookmarkClick = { viewModel.removeBookmark(lesson) },
                    )
                }
            }
        }
    }
}
