package com.eztech.feature.home.presentation.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.usecase.recommendation.GetRecommendationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val getRecommendations: GetRecommendationsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState: StateFlow<RecommendationsUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadRecommendations()
    }

    fun retry() = loadRecommendations()

    private fun loadRecommendations() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getRecommendations(maxResults = MAX_RECOMMENDATIONS).collect { result ->
                _uiState.update { state ->
                    when (result) {
                        Resource.Loading -> state.copy(
                            isLoading = state.recommendations.isEmpty(),
                            errorMessage = null,
                        )
                        is Resource.Success -> state.copy(
                            recommendations = result.data,
                            isLoading = false,
                            errorMessage = null,
                        )
                        is Resource.Error -> state.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val MAX_RECOMMENDATIONS = 12
    }
}
