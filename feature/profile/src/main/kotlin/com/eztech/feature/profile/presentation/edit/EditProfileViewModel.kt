package com.eztech.feature.profile.presentation.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var userId: String? = null

    init {
        loadProfile()
    }

    fun onNameChanged(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                errorMessage = null,
                successMessage = null,
                saved = false,
            )
        }
    }

    fun onAvatarUrlChanged(avatarUrl: String) {
        _uiState.update {
            it.copy(
                avatarUrlInput = avatarUrl,
                errorMessage = null,
                successMessage = null,
                saved = false,
            )
        }
    }

    fun save() {
        val currentUserId = userId ?: return
        val name = _uiState.value.name.trim()
        val avatarUrl = _uiState.value.avatarUrlInput.trim()
        if (name.length < EditProfileUiState.MIN_NAME_LENGTH) {
            _uiState.update {
                it.copy(errorMessage = "Display name must be at least 2 characters.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = userRepository.updateProfile(currentUserId, name)) {
                is Resource.Success -> when (val avatarResult = userRepository.updateAvatarUrl(currentUserId, avatarUrl)) {
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            avatarUrl = avatarResult.data.ifBlank { null },
                            avatarUrlInput = avatarResult.data,
                            isSaving = false,
                            saved = true,
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = avatarResult.message,
                        )
                    }
                    Resource.Loading -> Unit
                }
                is Resource.Error -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.message,
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val authUser = authRepository.observeCurrentUser().first()
            if (authUser == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Sign in to edit your profile.",
                    )
                }
                return@launch
            }
            userId = authUser.uid
            userRepository.observeUserProfile(authUser.uid).collectLatest { result ->
                when (result) {
                    Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            name = result.data.name,
                            email = result.data.email,
                            avatarUrl = result.data.avatarUrl,
                            avatarUrlInput = result.data.avatarUrl.orEmpty(),
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }
}
