package com.eztech.feature.profile.presentation.edit

data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val avatarUrlInput: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val saved: Boolean = false,
) {
    val canSave: Boolean
        get() = name.trim().length >= MIN_NAME_LENGTH && !isSaving

    companion object {
        const val MIN_NAME_LENGTH = 2
    }
}
