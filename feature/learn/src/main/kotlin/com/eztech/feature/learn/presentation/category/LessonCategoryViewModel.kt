package com.eztech.feature.learn.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.model.LessonContentType
import com.eztech.core.domain.usecase.GetLessonCategoriesUseCase
import com.eztech.core.domain.usecase.GetLessonsByTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LessonCategoryViewModel @Inject constructor(
    private val getLessonCategories: GetLessonCategoriesUseCase,
    private val getLessonsByType: GetLessonsByTypeUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LessonCategoryUiState())
    val uiState: StateFlow<LessonCategoryUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadCategories()
    }

    fun retry() = loadCategories()

    fun selectTab(tab: LearnTab) {
        _uiState.update { current -> current.copy(selectedTab = tab) }
    }

    private fun loadCategories() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            combine(
                getLessonCategories(PYTHON_LANGUAGE_ID),
                getLessonsByType(
                    languageId = PYTHON_LANGUAGE_ID,
                    type = LessonContentType.VIDEO,
                ),
            ) { categoriesResource, videosResource ->
                categoriesResource to videosResource
            }.collect { (categoriesResource, videosResource) ->
                _uiState.update { current ->
                    val categoryError = categoriesResource as? Resource.Error
                    val videoError = videosResource as? Resource.Error
                    when {
                        categoryError != null || videoError != null -> current.copy(
                            isLoading = false,
                            errorMessage = categoryError?.message ?: videoError?.message,
                        )

                        categoriesResource is Resource.Loading ||
                            videosResource is Resource.Loading -> current.copy(
                            isLoading = true,
                            errorMessage = null,
                        )

                        categoriesResource is Resource.Success &&
                            videosResource is Resource.Success -> current.copy(
                            isLoading = false,
                            categories = categoriesResource.data,
                            videoLessons = videosResource.data,
                            errorMessage = null,
                        )

                        else -> current
                    }
                }
            }
        }
    }

    companion object {
        const val PYTHON_LANGUAGE_ID = "python"
    }
}
