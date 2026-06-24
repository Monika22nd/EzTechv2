package com.eztech.feature.problems.navigation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.eztech.feature.problems.presentation.detail.ProblemDetailScreen
import com.eztech.feature.problems.presentation.list.ProblemListScreen
import com.eztech.feature.problems.presentation.solve.ProblemSolveScreen

object ProblemsRoutes {
    const val Root = "problems"
    const val ProblemIdArg = "problemId"
    const val Detail = "$Root/{$ProblemIdArg}"
    const val Solve = "$Root/{$ProblemIdArg}/solve"

    fun detail(problemId: String): String = "$Root/${Uri.encode(problemId)}"

    fun solve(problemId: String): String = "$Root/${Uri.encode(problemId)}/solve"
}

fun NavGraphBuilder.problemsGraph(navController: NavHostController) {
    composable(ProblemsRoutes.Root) {
        ProblemListScreen(
            onProblemClick = { problemId ->
                navController.navigate(ProblemsRoutes.detail(problemId))
            },
        )
    }
    composable(
        route = ProblemsRoutes.Detail,
        arguments = listOf(
            navArgument(ProblemsRoutes.ProblemIdArg) { type = NavType.StringType },
        ),
    ) {
        ProblemDetailScreen(
            onBackClick = navController::popBackStack,
            onSolveClick = { problemId ->
                navController.navigate(ProblemsRoutes.solve(problemId))
            },
        )
    }
    composable(
        route = ProblemsRoutes.Solve,
        arguments = listOf(
            navArgument(ProblemsRoutes.ProblemIdArg) { type = NavType.StringType },
        ),
    ) {
        ProblemSolveScreen(onBackClick = navController::popBackStack)
    }
}
