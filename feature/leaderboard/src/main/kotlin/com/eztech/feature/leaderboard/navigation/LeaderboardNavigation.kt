package com.eztech.feature.leaderboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.eztech.feature.leaderboard.presentation.screen.LeaderboardScreen

object LeaderboardRoutes {
    const val Root = "leaderboard"
}

fun NavGraphBuilder.leaderboardGraph(navController: NavHostController) {
    composable(LeaderboardRoutes.Root) {
        LeaderboardScreen()
    }
}
