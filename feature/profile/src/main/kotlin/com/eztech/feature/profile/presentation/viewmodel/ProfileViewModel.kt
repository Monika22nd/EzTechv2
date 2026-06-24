package com.eztech.feature.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.User
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.repository.UserRepository
import com.eztech.core.domain.usecase.gamification.GetUserBadgesUseCase
import com.eztech.core.domain.usecase.gamification.RecordDailyLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val badges: List<Badge> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val newlyUnlockedBadge: Badge? = null, // for animation trigger
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val getUserBadgesUseCase: GetUserBadgesUseCase,
    private val recordDailyLoginUseCase: RecordDailyLoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                if (user == null) {
                    _uiState.value = ProfileUiState(isLoading = false)
                    return@collect
                }
                userRepository.observeUserProfile(user.uid).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                        is Resource.Success -> {
                            val fullUser = resource.data
                            loadBadges(fullUser)
                            recordDailyLogin(fullUser)
                        }
                        is Resource.Error -> _uiState.value = _uiState.value.copy(
                            isLoading = false, error = resource.message,
                        )
                    }
                }
            }
        }
    }

    private fun loadBadges(user: User) {
        viewModelScope.launch {
            val result = getUserBadgesUseCase(user.uid)
            val badges = if (result is Resource.Success) result.data else emptyList()
            _uiState.value = _uiState.value.copy(
                user = user, badges = badges, isLoading = false,
            )
        }
    }

    private fun recordDailyLogin(user: User) {
        viewModelScope.launch {
            recordDailyLoginUseCase(userId = user.uid, lastLoginDate = user.lastLoginDate)
        }
    }

    fun onBadgeAnimationDone() {
        _uiState.value = _uiState.value.copy(newlyUnlockedBadge = null)
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
