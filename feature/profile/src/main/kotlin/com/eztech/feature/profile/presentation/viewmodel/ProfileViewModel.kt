package com.eztech.feature.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.User
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.repository.UserRepository
import com.eztech.core.domain.usecase.gamification.GetUserBadgesUseCase
import com.eztech.core.domain.usecase.gamification.GetLeaderboardUseCase
import com.eztech.core.domain.usecase.gamification.RecordDailyLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate

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
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private var dailyLoginRequestKey: String? = null

    init {
        loadProfile()
        observeRank()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collectLatest { user ->
                if (user == null) {
                    _uiState.value = ProfileUiState(isLoading = false)
                    return@collectLatest
                }
                userRepository.observeUserProfile(user.uid).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                        is Resource.Success -> {
                            val fullUser = resource.data
                            loadBadges(fullUser)
                            recordDailyLogin(fullUser.uid)
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

    private fun recordDailyLogin(userId: String) {
        val requestKey = "$userId:${LocalDate.now()}"
        if (dailyLoginRequestKey == requestKey) return
        dailyLoginRequestKey = requestKey
        viewModelScope.launch {
            val result = recordDailyLoginUseCase(userId)
            if (result is Resource.Success) {
                _uiState.value = _uiState.value.copy(
                    newlyUnlockedBadge = result.data.newlyUnlockedBadges.firstOrNull(),
                )
            } else if (result is Resource.Error) {
                dailyLoginRequestKey = null
            }
        }
    }

    private fun observeRank() {
        viewModelScope.launch {
            combine(
                authRepository.observeCurrentUser(),
                getLeaderboardUseCase(),
            ) { user, leaderboard ->
                if (user == null || leaderboard !is Resource.Success) return@combine 0
                leaderboard.data.firstOrNull { entry -> entry.userId == user.uid }?.rank ?: 0
            }.collect { rank ->
                _uiState.update { state ->
                    state.copy(user = state.user?.copy(rank = rank))
                }
            }
        }
    }

    fun onBadgeAnimationDone() {
        _uiState.value = _uiState.value.copy(newlyUnlockedBadge = null)
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
