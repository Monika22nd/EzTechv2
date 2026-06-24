package com.eztech.core.common

sealed interface UiEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
    ) : UiEvent

    data object NavigateBack : UiEvent
}

