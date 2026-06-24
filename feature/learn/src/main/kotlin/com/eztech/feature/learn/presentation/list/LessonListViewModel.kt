package com.eztech.feature.learn.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.usecase.GetLessonsByCategoryUseCase
import com.eztech.feature.learn.navigation.LearnRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LessonListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLessonsByCategory: GetLessonsByCategoryUseCase,
) : ViewModel() {
    private val languageId = savedStateHandle.get<String>(LearnRoutes.LanguageIdArg).orEmpty()
    private val categoryId = savedStateHandle.get<String>(LearnRoutes.CategoryIdArg).orEmpty()
    private val categoryName = savedStateHandle.get<String>(LearnRoutes.CategoryNameArg)
        ?.takeIf(String::isNotBlank)
        ?: "Lessons"

    private val _uiState = MutableStateFlow(
        LessonListUiState(categoryName = categoryName),
    )
    val uiState: StateFlow<LessonListUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadLessons()
    }

    fun retry() = loadLessons()

    private fun loadLessons() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getLessonsByCategory(
                languageId = languageId,
                categoryId = categoryId,
            ).collect { resource ->
                _uiState.update { current ->
                    when (resource) {
                        Resource.Loading -> current.copy(
                            isLoading = true,
                            errorMessage = null,
                        )

                        is Resource.Success -> current.copy(
                            isLoading = false,
                            lessons = resource.data,
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
}
