package com.eztech.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.usecase.GetDashboardSummaryUseCase
import com.eztech.core.domain.usecase.recommendation.GetRecommendationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home dashboard.
 *
 * It keeps dashboard summary and recommendation cards in separate jobs so a recommendation failure
 * does not block the main dashboard statistics from rendering.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDashboardSummary: GetDashboardSummaryUseCase,
    private val getRecommendations: GetRecommendationsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var recommendationJob: Job? = null

    init {
        loadDashboard()
        loadRecommendations()
    }

    /** Reloads both dashboard and recommendation streams after a visible error state. */
    fun retry() {
        loadDashboard()
        loadRecommendations()
    }

    /** Observes summary data such as level, lesson counts, solved problems, and next activity. */
    private fun loadDashboard() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getDashboardSummary().collect { result ->
                _uiState.update { current ->
                    when (result) {
                        Resource.Loading -> current.copy(
                            isLoading = current.summary == null,
                            errorMessage = null,
                        )
                        is Resource.Success -> current.copy(
                            summary = result.data,
                            isLoading = false,
                            errorMessage = null,
                        )
                        is Resource.Error -> current.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    /** Observes recommendation cards and stats shown in the Home recommendation section. */
    private fun loadRecommendations() {
        recommendationJob?.cancel()
        recommendationJob = viewModelScope.launch {
            getRecommendations(maxResults = MAX_RECOMMENDATIONS).collect { result ->
                _uiState.update { current ->
                    when (result) {
                        Resource.Loading -> current.copy(
                            isLoadingRecommendations = current.recommendations.isEmpty(),
                            recommendationErrorMessage = null,
                        )
                        is Resource.Success -> current.copy(
                            recommendations = result.data.recommendations,
                            recommendationStats = result.data.stats,
                            isLoadingRecommendations = false,
                            recommendationErrorMessage = null,
                        )
                        is Resource.Error -> current.copy(
                            isLoadingRecommendations = false,
                            recommendationErrorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val MAX_RECOMMENDATIONS = 5
    }
}
