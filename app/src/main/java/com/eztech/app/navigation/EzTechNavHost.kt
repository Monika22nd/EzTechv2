package com.eztech.app.navigation

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eztech.feature.auth.navigation.AuthRoutes
import com.eztech.feature.auth.navigation.authGraph
import com.eztech.feature.ide.navigation.ideGraph
import com.eztech.feature.learn.navigation.learnGraph
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
        learnGraph(navController)
        ideGraph()
        leaderboardGraph(
            onProblemsClick = { navController.navigate(ProblemsRoutes.Root) },
        )
        profileGraph(navController)
        problemsGraph(navController)
    }
}
