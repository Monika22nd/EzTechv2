package com.eztech.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.eztech.feature.auth.presentation.ForgotPasswordScreen
import com.eztech.feature.auth.presentation.LoginScreen
import com.eztech.feature.auth.presentation.RegisterScreen

object AuthRoutes {
    const val Login = "auth/login"
    const val Register = "auth/register"
    const val ForgotPassword = "auth/forgot-password"

    fun contains(route: String?): Boolean = route in setOf(Login, Register, ForgotPassword)
}

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onAuthenticated: () -> Unit,
) {
    composable(AuthRoutes.Login) {
        LoginScreen(
            onAuthenticated = onAuthenticated,
            onRegisterClick = { navController.navigate(AuthRoutes.Register) },
            onForgotPasswordClick = { navController.navigate(AuthRoutes.ForgotPassword) },
        )
    }
    composable(AuthRoutes.Register) {
        RegisterScreen(
            onAuthenticated = onAuthenticated,
            onBackToLogin = navController::popBackStack,
        )
    }
    composable(AuthRoutes.ForgotPassword) {
        ForgotPasswordScreen(
            onBackToLogin = navController::popBackStack,
        )
    }
}
