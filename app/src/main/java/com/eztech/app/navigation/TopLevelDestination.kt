package com.eztech.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.eztech.core.ui.component.EzTechBottomBarItem
import com.eztech.feature.home.navigation.HomeRoutes
import com.eztech.feature.problems.navigation.ProblemsRoutes

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Home(
        route = HomeRoutes.Root,
        label = "Home",
        icon = Icons.Rounded.Home,
    ),
    Learn(
        route = "learn",
        label = "Learn",
        icon = Icons.AutoMirrored.Rounded.MenuBook,
    ),
    Ide(
        route = "ide",
        label = "IDE",
        icon = Icons.Rounded.Code,
    ),
    Problems(
        route = ProblemsRoutes.Root,
        label = "Problems",
        icon = Icons.AutoMirrored.Rounded.Assignment,
    ),
    Leaderboard(
        route = "leaderboard",
        label = "Rank",
        icon = Icons.Rounded.EmojiEvents,
    ),
    Profile(
        route = "profile",
        label = "Me",
        icon = Icons.Rounded.Person,
    ),
}

fun TopLevelDestination.toBottomBarItem(): EzTechBottomBarItem =
    EzTechBottomBarItem(
        route = route,
        label = label,
        icon = icon,
    )
