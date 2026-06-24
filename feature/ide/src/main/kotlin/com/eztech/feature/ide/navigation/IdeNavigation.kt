package com.eztech.feature.ide.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.eztech.feature.ide.presentation.IdeScreen

object IdeRoutes {
    const val Root = "ide"
}

fun NavGraphBuilder.ideGraph() {
    composable(IdeRoutes.Root) {
        IdeScreen()
    }
}
