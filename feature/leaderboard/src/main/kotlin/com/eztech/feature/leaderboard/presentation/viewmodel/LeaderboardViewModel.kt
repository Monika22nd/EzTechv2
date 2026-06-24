package com.eztech.feature.leaderboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.domain.model.LeaderboardEntry
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.usecase.gamification.GetLeaderboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = true,
    val currentUserId: String? = null,
    val error: String? = null,
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init { observeLeaderboard() }

    private fun observeLeaderboard() {
        viewModelScope.launch {
            combine(
                getLeaderboardUseCase(),
                authRepository.observeCurrentUser(),
            ) { entries, user ->
                val uid = user?.uid
                LeaderboardUiState(
                    entries = entries.map { it.copy(isCurrentUser = it.userId == uid) },
                    isLoading = false,
                    currentUserId = uid,
                )
            }.collect { _uiState.value = it }
        }
    }
}
