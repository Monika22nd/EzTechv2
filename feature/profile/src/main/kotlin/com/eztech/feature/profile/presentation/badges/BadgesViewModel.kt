package com.eztech.feature.profile.presentation.badges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.usecase.gamification.GetUserBadgesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BadgesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getUserBadgesUseCase: GetUserBadgesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BadgesUiState())
    val uiState: StateFlow<BadgesUiState> = _uiState.asStateFlow()

    init {
        loadBadges()
    }

    fun selectFilter(filter: BadgeFilter) {
        _uiState.update { state -> state.copy(selectedFilter = filter) }
    }

    fun retry() = loadBadges()

    private fun loadBadges() {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true, errorMessage = null) }
            val user = authRepository.observeCurrentUser().first()
            if (user == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Sign in to view badges.",
                    )
                }
                return@launch
            }

            when (val result = getUserBadgesUseCase(user.uid)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        badges = result.data,
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
                Resource.Loading -> Unit
            }
        }
    }
}
