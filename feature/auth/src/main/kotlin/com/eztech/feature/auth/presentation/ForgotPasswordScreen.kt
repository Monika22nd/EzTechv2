package com.eztech.feature.auth.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.ui.component.EzTechButton
import com.eztech.feature.auth.presentation.component.AuthMessage
import com.eztech.feature.auth.presentation.component.AuthScreenLayout

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AuthScreenLayout(
        title = "Reset your password",
        subtitle = "We will send a password reset link to your email.",
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
                imeAction = ImeAction.Done,
            ),
        )
        state.errorMessage?.let { message ->
            AuthMessage(message = message, isError = true)
        }
        if (state.isResetEmailSent) {
            AuthMessage(
                message = "Reset email sent. Check your inbox.",
                isError = false,
            )
        }
        EzTechButton(
            text = if (state.isLoading) "Sending..." else "Send reset link",
            onClick = viewModel::sendPasswordReset,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            leadingIcon = {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Rounded.MarkEmailRead, contentDescription = null)
                }
            },
        )
        TextButton(onClick = onBackToLogin) {
            Text("Back to sign in")
        }
    }
}
