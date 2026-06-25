package com.eztech.feature.learn.presentation.category

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonCategory
import com.eztech.core.domain.model.LessonContentType
import com.eztech.core.ui.component.EzTechButton
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.learn.presentation.component.CategoryItem
import com.eztech.feature.learn.presentation.component.LearnTopBar
import com.eztech.feature.learn.presentation.component.formatDuration

@Composable
fun LessonCategoryScreen(
    onCategoryClick: (LessonCategory) -> Unit,
    onVideoClick: (Lesson) -> Unit,
    onBookmarksClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LessonCategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            LearnTopBar(
                title = "Learn Python",
                actions = {
                    IconButton(onClick = onBookmarksClick) {
                        Icon(
                            imageVector = Icons.Rounded.Bookmarks,
                            contentDescription = "Bookmarks",
                        )
                    }
                },
            )
        },
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

            state.categories.isEmpty() && state.videoLessons.isEmpty() -> EzTechEmptyState(
                title = "No lessons yet",
                message = "Python lessons will appear here.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(EzTechDimens.ScreenPadding),
            )

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                LearnTabs(
                    selectedTab = state.selectedTab,
                    onTabSelected = viewModel::selectTab,
                )
                when (state.selectedTab) {
                    LearnTab.Videos -> VideoLessonsContent(
                        lessons = state.videoLessons,
                        onVideoClick = onVideoClick,
                        modifier = Modifier.fillMaxSize(),
                    )

                    LearnTab.Tutorials -> TutorialCategoriesContent(
                        categories = state.categories
                            .filter { category -> category.type == LessonContentType.TUTORIAL },
                        onCategoryClick = onCategoryClick,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun LearnTabs(
    selectedTab: LearnTab,
    onTabSelected: (LearnTab) -> Unit,
) {
    TabRow(selectedTabIndex = selectedTab.ordinal) {
        Tab(
            selected = selectedTab == LearnTab.Videos,
            onClick = { onTabSelected(LearnTab.Videos) },
            text = { Text(text = "Videos") },
        )
        Tab(
            selected = selectedTab == LearnTab.Tutorials,
            onClick = { onTabSelected(LearnTab.Tutorials) },
            text = { Text(text = "Tutorials") },
        )
    }
}

@Composable
private fun VideoLessonsContent(
    lessons: List<Lesson>,
    onVideoClick: (Lesson) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = EzTechDimens.ScreenPadding,
            top = EzTechDimens.SpaceMedium,
            end = EzTechDimens.ScreenPadding,
            bottom = 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
    ) {
        item {
            LearnSectionHeader(
                title = "Corey Schafer video tutorials",
                subtitle = "${lessons.size} videos for Python and backend practice",
            )
        }
        if (lessons.isEmpty()) {
            item {
                EzTechEmptyState(
                    title = "No videos yet",
                    message = "Video tutorials will appear here after seed data is imported.",
                )
            }
        } else {
            items(
                items = lessons,
                key = Lesson::id,
            ) { lesson ->
                VideoLessonItem(
                    lesson = lesson,
                    onClick = { onVideoClick(lesson) },
                )
            }
        }
    }
}

@Composable
private fun TutorialCategoriesContent(
    categories: List<LessonCategory>,
    onCategoryClick: (LessonCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 164.dp),
        modifier = modifier,
        contentPadding = PaddingValues(
            start = EzTechDimens.ScreenPadding,
            top = EzTechDimens.SpaceMedium,
            end = EzTechDimens.ScreenPadding,
            bottom = 96.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            LearnSectionHeader(
                title = "Python tutorial topics",
                subtitle = "${categories.sumOf(LessonCategory::lessonCount)} written lessons",
            )
        }
        if (categories.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EzTechEmptyState(
                    title = "No tutorials yet",
                    message = "Tutorial topics will appear here after seed data is imported.",
                )
            }
        } else {
            items(
                items = categories,
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

@Composable
private fun LearnSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall),
        modifier = modifier.padding(bottom = EzTechDimens.SpaceSmall),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VideoLessonItem(
    lesson: Lesson,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EzTechDimens.SpaceMedium),
            horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall),
            ) {
                Text(
                    text = "${lesson.order}. ${lesson.title}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = videoMeta(lesson),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (lesson.watched) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Completed",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

private fun videoMeta(lesson: Lesson): String {
    val durationText = lesson.durationSeconds
        .takeIf { duration -> duration > 0 }
        ?.let(::formatDuration)
        ?: "Video"
    val sourceText = lesson.sourceName.ifBlank { "Corey Schafer" }
    return "$durationText | $sourceText"
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
