package com.eztech.app.navigation

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eztech.core.domain.model.LessonContentType
import com.eztech.feature.auth.navigation.AuthRoutes
import com.eztech.feature.auth.navigation.authGraph
import com.eztech.feature.home.navigation.HomeRoutes
import com.eztech.feature.home.navigation.homeGraph
import com.eztech.feature.ide.navigation.ideGraph
import com.eztech.feature.learn.navigation.learnGraph
import com.eztech.feature.learn.navigation.LearnRoutes
import com.eztech.feature.leaderboard.navigation.leaderboardGraph
import com.eztech.feature.profile.navigation.profileGraph
import com.eztech.feature.problems.navigation.problemsGraph
import com.eztech.feature.problems.navigation.ProblemsRoutes

@Composable
fun EzTechNavHost(
    navController: NavHostController,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AuthRoutes.Login,
        modifier = modifier,
    ) {
        authGraph(navController = navController, onAuthenticated = onAuthenticated)
        homeGraph(
            onLearnClick = {
                navController.navigateToTopLevelDestination(TopLevelDestination.Learn)
            },
            onIdeClick = {
                navController.navigateToTopLevelDestination(TopLevelDestination.Ide)
            },
            onProblemsClick = {
                navController.navigateToTopLevelDestination(TopLevelDestination.Problems)
            },
            onRecommendationsClick = {
                navController.navigate(HomeRoutes.Recommendations)
            },
            onBackClick = {
                navController.popBackStack()
            },
            onLessonClick = { lesson ->
                val route = if (lesson.type == LessonContentType.TUTORIAL) {
                    LearnRoutes.tutorialArticle(lesson.id)
                } else {
                    LearnRoutes.videoPlayer(lesson.id)
                }
                navController.navigate(route)
            },
            onProblemClick = { problemId ->
                navController.navigate(ProblemsRoutes.detail(problemId))
            },
        )
        learnGraph(navController)
        ideGraph()
        leaderboardGraph(
            onProblemsClick = { navController.navigate(ProblemsRoutes.Root) },
        )
        profileGraph(navController)
        problemsGraph(navController)
    }
}
