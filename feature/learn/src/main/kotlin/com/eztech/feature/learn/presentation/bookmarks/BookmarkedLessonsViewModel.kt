package com.eztech.feature.learn.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.usecase.GetBookmarkedLessonsUseCase
import com.eztech.core.domain.usecase.SetLessonBookmarkedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BookmarkedLessonsViewModel @Inject constructor(
    private val getBookmarkedLessons: GetBookmarkedLessonsUseCase,
    private val setLessonBookmarked: SetLessonBookmarkedUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookmarkedLessonsUiState())
    val uiState: StateFlow<BookmarkedLessonsUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadBookmarks()
    }

    fun retry() = loadBookmarks()

    fun removeBookmark(lesson: Lesson) {
        viewModelScope.launch {
            val user = authRepository.observeCurrentUser().first()
            if (user == null) {
                _uiState.update { it.copy(message = "Sign in to save bookmarks.") }
                return@launch
            }
            when (
                val result = setLessonBookmarked(
                    userId = user.uid,
                    lessonId = lesson.id,
                    bookmarked = false,
                )
            ) {
                is Resource.Success -> _uiState.update { state ->
                    state.copy(
                        lessons = state.lessons.filterNot { item -> item.id == lesson.id },
                        message = "Bookmark removed.",
                    )
                }
                is Resource.Error -> _uiState.update { it.copy(message = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun loadBookmarks() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getBookmarkedLessons(PYTHON_LANGUAGE_ID).collect { resource ->
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

    private companion object {
        const val PYTHON_LANGUAGE_ID = "python"
    }
}
