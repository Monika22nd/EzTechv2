package com.eztech.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<AuthEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun onNameChanged(value: String) = updateState { copy(name = value) }

    fun onEmailChanged(value: String) = updateState {
        copy(email = value, isResetEmailSent = false)
    }

    fun onPasswordChanged(value: String) = updateState { copy(password = value) }

    fun onConfirmPasswordChanged(value: String) = updateState { copy(confirmPassword = value) }

    fun togglePasswordVisibility() = updateState {
        copy(isPasswordVisible = !isPasswordVisible)
    }

    fun login() {
        val state = _uiState.value
        val validationError = validateCredentials(state.email, state.password)
        if (validationError != null) {
            showError(validationError)
            return
        }

        executeAuthAction {
            authRepository.login(state.email, state.password)
        }
    }

    fun register() {
        val state = _uiState.value
        val validationError = when {
            state.name.trim().length < 2 -> "Enter a name with at least 2 characters."
            state.password != state.confirmPassword -> "Passwords do not match."
            else -> validateCredentials(state.email, state.password)
        }
        if (validationError != null) {
            showError(validationError)
            return
        }

        executeAuthAction {
            authRepository.register(
                name = state.name,
                email = state.email,
                password = state.password,
            )
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.email
        if (!isValidEmail(email)) {
            showError("Enter a valid email address.")
            return
        }

        viewModelScope.launch {
            setLoading(true)
            when (val result = authRepository.sendPasswordReset(email)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        isResetEmailSent = true,
                    )
                }

                is Resource.Error -> showError(result.message)
                Resource.Loading -> setLoading(true)
            }
        }
    }

    private fun executeAuthAction(action: suspend () -> Resource<*>) {
        viewModelScope.launch {
            setLoading(true)
            when (val result = action()) {
                is Resource.Success -> {
                    setLoading(false)
                    eventChannel.send(AuthEvent.Authenticated)
                }

                is Resource.Error -> showError(result.message)
                Resource.Loading -> setLoading(true)
            }
        }
    }

    private fun validateCredentials(email: String, password: String): String? = when {
        !isValidEmail(email) -> "Enter a valid email address."
        password.length < MIN_PASSWORD_LENGTH -> "Password must have at least 6 characters."
        else -> null
    }

    private fun isValidEmail(email: String): Boolean {
        val value = email.trim()
        return value.contains('@') && value.substringAfter('@').contains('.')
    }

    private fun updateState(transform: AuthUiState.() -> AuthUiState) {
        _uiState.update { state -> transform(state).copy(errorMessage = null) }
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading, errorMessage = null) }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
