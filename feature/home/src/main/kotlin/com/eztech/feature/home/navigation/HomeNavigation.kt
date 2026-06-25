package com.eztech.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.eztech.core.domain.model.Lesson
import com.eztech.feature.home.presentation.HomeScreen
import com.eztech.feature.home.presentation.recommendation.RecommendationsScreen

object HomeRoutes {
    const val Root = "home"
    const val Recommendations = "home/recommendations"
}

fun NavGraphBuilder.homeGraph(
    onLearnClick: () -> Unit,
    onIdeClick: () -> Unit,
    onProblemsClick: () -> Unit,
    onRecommendationsClick: () -> Unit,
    onBackClick: () -> Unit,
    onLessonClick: (Lesson) -> Unit,
    onProblemClick: (String) -> Unit,
) {
    composable(HomeRoutes.Root) {
        HomeScreen(
            onLearnClick = onLearnClick,
            onIdeClick = onIdeClick,
            onProblemsClick = onProblemsClick,
            onRecommendationsClick = onRecommendationsClick,
            onLessonClick = onLessonClick,
            onProblemClick = onProblemClick,
        )
    }
    composable(HomeRoutes.Recommendations) {
        RecommendationsScreen(
            onBackClick = onBackClick,
            onLessonClick = onLessonClick,
            onProblemClick = onProblemClick,
        )
    }
}
