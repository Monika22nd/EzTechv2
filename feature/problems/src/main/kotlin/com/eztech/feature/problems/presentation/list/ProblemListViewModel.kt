package com.eztech.feature.problems.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.usecase.problem.GetProblemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProblemListViewModel @Inject constructor(
    private val getProblems: GetProblemsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProblemListUiState())
    val uiState: StateFlow<ProblemListUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadProblems()
    }

    fun selectDifficulty(difficulty: Difficulty?) {
        if (_uiState.value.selectedDifficulty == difficulty) return
        _uiState.update { state ->
            state.copy(selectedDifficulty = difficulty).withFilteredProblems()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(searchQuery = query).withFilteredProblems()
        }
    }

    fun selectTag(tag: String?) {
        if (_uiState.value.selectedTag == tag) return
        _uiState.update { state ->
            state.copy(selectedTag = tag).withFilteredProblems()
        }
    }

    fun selectSortOption(option: ProblemSortOption) {
        if (_uiState.value.sortOption == option) return
        _uiState.update { state ->
            state.copy(sortOption = option).withFilteredProblems()
        }
    }

    fun retry() = loadProblems()

    private fun loadProblems() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getProblems().collect { result ->
                _uiState.update { state ->
                    when (result) {
                        Resource.Loading -> state.copy(isLoading = true, errorMessage = null)
                        is Resource.Success -> state.copy(
                            allProblems = result.data,
                            availableTags = result.data
                                .flatMap(Problem::tags)
                                .map(String::trim)
                                .filter(String::isNotEmpty)
                                .distinct()
                                .sortedBy(String::lowercase),
                            isLoading = false,
                            errorMessage = null,
                        ).withFilteredProblems()
                        is Resource.Error -> state.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    private fun ProblemListUiState.withFilteredProblems(): ProblemListUiState {
        val query = searchQuery.trim()
        val filtered = allProblems
            .asSequence()
            .filter { problem ->
                selectedDifficulty == null || problem.difficulty == selectedDifficulty
            }
            .filter { problem ->
                selectedTag == null || problem.tags.any { tag ->
                    tag.equals(selectedTag, ignoreCase = true)
                }
            }
            .filter { problem ->
                query.isBlank() ||
                    problem.title.contains(query, ignoreCase = true) ||
                    problem.description.contains(query, ignoreCase = true) ||
                    problem.tags.any { tag -> tag.contains(query, ignoreCase = true) } ||
                    problem.order.toString() == query.removePrefix("#")
            }
            .toList()
            .sortedWith(sortOption.comparator)

        return copy(problems = filtered)
    }

    private val ProblemSortOption.comparator: Comparator<Problem>
        get() = when (this) {
            ProblemSortOption.ORDER -> compareByOrder()
            ProblemSortOption.EASY_FIRST -> compareBy<Problem> { problem ->
                problem.difficulty.rank
            }.then(compareByOrder())
            ProblemSortOption.HARD_FIRST -> compareByDescending<Problem> { problem ->
                problem.difficulty.rank
            }.then(compareByOrder())
        }

    private fun compareByOrder(): Comparator<Problem> =
        compareBy<Problem> { problem ->
            problem.order.takeIf { it > 0 } ?: Int.MAX_VALUE
        }.thenBy { problem -> problem.title }

    private val Difficulty.rank: Int
        get() = when (this) {
            Difficulty.EASY -> 0
            Difficulty.MEDIUM -> 1
            Difficulty.HARD -> 2
        }
}
