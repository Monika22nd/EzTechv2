package com.eztech.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eztech.app.EzTechAppViewModel
import com.eztech.app.SessionState
import com.eztech.core.ui.component.EzTechBottomBar
import com.eztech.feature.auth.navigation.AuthRoutes

@Composable
fun EzTechApp(
    viewModel: EzTechAppViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val destinations = TopLevelDestination.entries
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val showBottomBar = destinations.any { it.route == currentRoute }

    LaunchedEffect(sessionState, currentRoute) {
        if (sessionState == SessionState.SignedIn && AuthRoutes.contains(currentRoute)) {
            navController.navigateToMain()
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                EzTechBottomBar(
                    items = destinations.map(TopLevelDestination::toBottomBarItem),
                    selectedRoute = currentRoute,
                    onItemClick = { item ->
                        destinations
                            .firstOrNull { destination -> destination.route == item.route }
                            ?.let { destination ->
                                navController.navigateToTopLevelDestination(destination)
                            }
                    },
                )
            }
        },
    ) { innerPadding ->
        EzTechNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onAuthenticated = navController::navigateToMain,
        )
    }
}
