package com.eztech.feature.learn.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.usecase.GetLessonCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LessonCategoryViewModel @Inject constructor(
    private val getLessonCategories: GetLessonCategoriesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LessonCategoryUiState())
    val uiState: StateFlow<LessonCategoryUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadCategories()
    }

    fun retry() = loadCategories()

    private fun loadCategories() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getLessonCategories(PYTHON_LANGUAGE_ID).collect { resource ->
                _uiState.update { current ->
                    when (resource) {
                        Resource.Loading -> current.copy(
                            isLoading = true,
                            errorMessage = null,
                        )

                        is Resource.Success -> current.copy(
                            isLoading = false,
                            categories = resource.data,
                            errorMessage = null,
                        )

                        is Resource.Error -> current.copy(
                            isLoading = false,
                            errorMessage = resource.message,
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val PYTHON_LANGUAGE_ID = "python"
    }
}
