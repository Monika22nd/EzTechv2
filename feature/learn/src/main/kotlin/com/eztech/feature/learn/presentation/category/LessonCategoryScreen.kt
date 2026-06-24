package com.eztech.feature.learn.presentation.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.domain.model.LessonCategory
import com.eztech.core.ui.component.EzTechButton
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.learn.presentation.component.CategoryItem
import com.eztech.feature.learn.presentation.component.LearnTopBar

@Composable
fun LessonCategoryScreen(
    onCategoryClick: (LessonCategory) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LessonCategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = { LearnTopBar(title = "Learn Python") },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            state.errorMessage != null -> ErrorContent(
                message = state.errorMessage.orEmpty(),
                onRetry = viewModel::retry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            state.categories.isEmpty() -> EzTechEmptyState(
                title = "No lessons yet",
                message = "Python lesson categories will appear here.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(EzTechDimens.ScreenPadding),
            )

            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 164.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = EzTechDimens.ScreenPadding,
                    vertical = EzTechDimens.SpaceMedium,
                ),
                horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
                verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall),
                        modifier = Modifier.padding(bottom = EzTechDimens.SpaceSmall),
                    ) {
                        Text(
                            text = "Python learning path",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "${state.categories.sumOf(LessonCategory::lessonCount)} video lessons",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(
                    items = state.categories,
                    key = LessonCategory::id,
                ) { category ->
                    CategoryItem(
                        category = category,
                        onClick = { onCategoryClick(category) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(EzTechDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(
            space = EzTechDimens.SpaceLarge,
            alignment = Alignment.CenterVertically,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EzTechEmptyState(
            title = "Could not load lessons",
            message = message,
        )
        EzTechButton(text = "Try again", onClick = onRetry)
    }
}
