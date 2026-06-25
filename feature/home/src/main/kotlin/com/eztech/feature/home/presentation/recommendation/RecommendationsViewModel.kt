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

/**
 * ViewModel for the full Recommendations page.
 *
 * It requests more cards than Home and exposes both the ranked list and the recommendation stats
 * card so users can understand why the list changes over time.
 */
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

    /** Manual retry entry point used by the empty/error state action. */
    fun retry() = loadRecommendations()

    /** Starts a fresh recommendation stream and maps Resource states into Compose UI state. */
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
                            recommendations = result.data.recommendations,
                            stats = result.data.stats,
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
