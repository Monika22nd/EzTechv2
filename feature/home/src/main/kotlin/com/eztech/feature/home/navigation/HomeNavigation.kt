package com.eztech.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.eztech.core.domain.model.Lesson
import com.eztech.feature.home.presentation.HomeScreen

object HomeRoutes {
    const val Root = "home"
}

fun NavGraphBuilder.homeGraph(
    onLearnClick: () -> Unit,
    onIdeClick: () -> Unit,
    onProblemsClick: () -> Unit,
    onLessonClick: (Lesson) -> Unit,
    onProblemClick: (String) -> Unit,
) {
    composable(HomeRoutes.Root) {
        HomeScreen(
            onLearnClick = onLearnClick,
            onIdeClick = onIdeClick,
            onProblemsClick = onProblemsClick,
            onLessonClick = onLessonClick,
            onProblemClick = onProblemClick,
        )
    }
}
