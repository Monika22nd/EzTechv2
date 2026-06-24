package com.eztech.feature.auth.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.ui.component.EzTechButton
import com.eztech.feature.auth.presentation.component.AuthMessage
import com.eztech.feature.auth.presentation.component.AuthPasswordField
import com.eztech.feature.auth.presentation.component.AuthScreenLayout

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event == AuthEvent.Authenticated) onAuthenticated()
        }
    }

    AuthScreenLayout(
        title = "Welcome back",
        subtitle = "Sign in to continue learning and coding.",
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )
        AuthPasswordField(
            value = state.password,
            onValueChange = viewModel::onPasswordChanged,
            label = "Password",
            isVisible = state.isPasswordVisible,
            onVisibilityClick = viewModel::togglePasswordVisibility,
        )
        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Forgot password?")
        }
        state.errorMessage?.let { message ->
            AuthMessage(message = message, isError = true)
        }
        EzTechButton(
            text = if (state.isLoading) "Signing in..." else "Sign in",
            onClick = viewModel::login,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            leadingIcon = {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.AutoMirrored.Rounded.Login, contentDescription = null)
                }
            },
        )
        TextButton(
            onClick = onRegisterClick,
            enabled = !state.isLoading,
        ) {
            Text("New to EzTech? Create an account")
        }
    }
}
