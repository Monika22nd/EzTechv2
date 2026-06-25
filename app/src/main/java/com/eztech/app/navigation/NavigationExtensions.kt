package com.eztech.app.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.eztech.feature.auth.navigation.AuthRoutes

fun NavController.navigateToTopLevelDestination(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.navigateToMain() {
    navigate(TopLevelDestination.Home.route) {
        popUpTo(AuthRoutes.Login) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

fun NavController.navigateToAuth() {
    navigate(AuthRoutes.Login) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
