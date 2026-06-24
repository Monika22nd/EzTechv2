package com.eztech.feature.auth.presentation

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isResetEmailSent: Boolean = false,
)

sealed interface AuthEvent {
    data object Authenticated : AuthEvent
}
