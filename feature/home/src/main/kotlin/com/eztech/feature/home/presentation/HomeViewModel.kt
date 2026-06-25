package com.eztech.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.usecase.GetDashboardSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDashboardSummary: GetDashboardSummaryUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadDashboard()
    }

    fun retry() = loadDashboard()

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
                        is Resource.Success -> HomeUiState(
                            summary = result.data,
                            isLoading = false,
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
}
