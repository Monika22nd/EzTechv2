package com.eztech.feature.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.eztech.feature.profile.presentation.screen.ProfileScreen

object ProfileRoutes {
    const val Root = "profile"
}

fun NavGraphBuilder.profileGraph(navController: NavHostController) {
    composable(ProfileRoutes.Root) {
        ProfileScreen()
    }
}
