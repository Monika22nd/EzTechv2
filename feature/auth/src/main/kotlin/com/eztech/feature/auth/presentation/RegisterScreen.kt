package com.eztech.feature.auth.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonAdd
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
fun RegisterScreen(
    onAuthenticated: () -> Unit,
    onBackToLogin: () -> Unit,
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
        title = "Create your account",
        subtitle = "Save your progress, EXP, and solved problems.",
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Display name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
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
            imeAction = ImeAction.Next,
        )
        AuthPasswordField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChanged,
            label = "Confirm password",
            isVisible = state.isPasswordVisible,
            onVisibilityClick = viewModel::togglePasswordVisibility,
        )
        state.errorMessage?.let { message ->
            AuthMessage(message = message, isError = true)
        }
        EzTechButton(
            text = if (state.isLoading) "Creating account..." else "Create account",
            onClick = viewModel::register,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            leadingIcon = {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Rounded.PersonAdd, contentDescription = null)
                }
            },
        )
        TextButton(
            onClick = onBackToLogin,
            enabled = !state.isLoading,
        ) {
            Text("Already have an account? Sign in")
        }
    }
}
