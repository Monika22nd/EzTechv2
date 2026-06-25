package com.eztech.feature.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.eztech.feature.profile.presentation.badges.BadgesScreen
import com.eztech.feature.profile.presentation.edit.EditProfileScreen
import com.eztech.feature.profile.presentation.screen.ProfileScreen
import com.eztech.feature.profile.presentation.settings.SettingsScreen

object ProfileRoutes {
    const val Root = "profile"
    const val Badges = "profile/badges"
    const val Edit = "profile/edit"
    const val Settings = "profile/settings"
}

fun NavGraphBuilder.profileGraph(navController: NavHostController) {
    composable(ProfileRoutes.Root) {
        ProfileScreen(
            onEditProfileClick = {
                navController.navigate(ProfileRoutes.Edit)
            },
            onSettingsClick = {
                navController.navigate(ProfileRoutes.Settings)
            },
            onViewAllBadgesClick = {
                navController.navigate(ProfileRoutes.Badges)
            },
        )
    }
    composable(ProfileRoutes.Badges) {
        BadgesScreen(onBackClick = navController::popBackStack)
    }
    composable(ProfileRoutes.Edit) {
        EditProfileScreen(onBackClick = navController::popBackStack)
    }
    composable(ProfileRoutes.Settings) {
        SettingsScreen(onBackClick = navController::popBackStack)
    }
}
