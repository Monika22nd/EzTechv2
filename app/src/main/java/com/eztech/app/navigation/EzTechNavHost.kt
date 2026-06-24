package com.eztech.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.eztech.core.ui.component.EzTechButton
import com.eztech.core.ui.component.EzTechCard
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.auth.navigation.AuthRoutes
import com.eztech.feature.auth.navigation.authGraph
import com.eztech.feature.learn.navigation.learnGraph

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
        authGraph(
            navController = navController,
            onAuthenticated = onAuthenticated,
        )
        learnGraph(navController)
        composable(TopLevelDestination.Ide.route) {
            PlaceholderScreen(
                title = "Python IDE",
                description = "Write and run Python code directly inside EzTech.",
                actionLabel = "Open editor",
            )
        }
        composable(TopLevelDestination.Leaderboard.route) {
            PlaceholderScreen(
                title = "Leaderboard",
                description = "Compare EXP, solved problems, and learning streaks.",
                actionLabel = "View ranking",
            )
        }
        composable(TopLevelDestination.Profile.route) {
            PlaceholderScreen(
                title = "My Profile",
                description = "Manage account details, badges, settings, and progress.",
                actionLabel = "Edit profile",
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
    actionLabel: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(EzTechDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(
                space = EzTechDimens.SpaceLarge,
                alignment = Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EzTechCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(EzTechDimens.SpaceLarge),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    EzTechButton(
                        text = actionLabel,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                            )
                        },
                        onClick = {},
                    )
                }
            }
        }
    }
}
