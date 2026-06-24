package com.eztech.app.navigation

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eztech.feature.auth.navigation.AuthRoutes
import com.eztech.feature.auth.navigation.authGraph
import com.eztech.feature.learn.navigation.learnGraph
import com.eztech.feature.leaderboard.navigation.leaderboardGraph
import com.eztech.feature.profile.navigation.profileGraph

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
        composable(TopLevelDestination.Ide.route) {
            // IDE placeholder — Phase 3
            com.eztech.app.navigation.IdeComingSoonScreen()
        }
        leaderboardGraph(navController)
        profileGraph(navController)
    }
}
