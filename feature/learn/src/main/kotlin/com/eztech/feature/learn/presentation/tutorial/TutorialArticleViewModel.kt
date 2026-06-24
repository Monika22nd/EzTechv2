package com.eztech.feature.learn.presentation.tutorial

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.usecase.GetLessonUseCase
import com.eztech.core.domain.usecase.MarkLessonWatchedUseCase
import com.eztech.feature.learn.navigation.LearnRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TutorialArticleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLesson: GetLessonUseCase,
    private val markLessonWatched: MarkLessonWatchedUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val lessonId = savedStateHandle.get<String>(LearnRoutes.LessonIdArg).orEmpty()
    private val _uiState = MutableStateFlow(TutorialArticleUiState())
    val uiState: StateFlow<TutorialArticleUiState> = _uiState.asStateFlow()
    private var completionHandled = false

    init {
        observeLesson()
    }

    fun markComplete() {
        val lesson = _uiState.value.lesson ?: return
        if (lesson.watched || completionHandled) return
        completionHandled = true

        viewModelScope.launch {
            val user = authRepository.observeCurrentUser().first()
            if (user == null) {
                completionHandled = false
                _uiState.update { it.copy(message = "Sign in to save lesson progress.") }
                return@launch
            }

            when (val result = markLessonWatched(user.uid, lesson.id)) {
                is Resource.Success -> _uiState.update {
                    it.copy(message = "Tutorial completed.")
                }

                is Resource.Error -> {
                    completionHandled = false
                    _uiState.update { it.copy(message = result.message) }
                }

                Resource.Loading -> Unit
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun observeLesson() {
        viewModelScope.launch {
            getLesson(lessonId).collect { resource ->
                _uiState.update { current ->
                    when (resource) {
                        Resource.Loading -> current.copy(
                            isLoading = true,
                            errorMessage = null,
                        )

                        is Resource.Success -> current.copy(
                            isLoading = false,
                            lesson = resource.data,
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
