package com.eztech.feature.problems.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.PythonProblemCurriculum
import com.eztech.core.domain.usecase.problem.GetProblemsUseCase
import com.eztech.feature.problems.presentation.model.ProblemTypeCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Problems list.
 *
 * It owns search, difficulty filters, type filters, and sort order. Filtering is performed in
 * memory after repository loading so all controls update immediately without new Firestore reads.
 */
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

    /** Applies or clears the selected Easy/Medium/Hard filter. */
    fun selectDifficulty(difficulty: Difficulty?) {
        if (_uiState.value.selectedDifficulty == difficulty) return
        _uiState.update { state ->
            state.copy(selectedDifficulty = difficulty).withFilteredProblems()
        }
    }

    /** Updates keyword search across title, description, tags, order, and curriculum labels. */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(searchQuery = query).withFilteredProblems()
        }
    }

    /** Applies a topic/curriculum filter such as loops, strings, lists, or algorithms. */
    fun selectProblemType(problemType: String?) {
        if (_uiState.value.selectedProblemType == problemType) return
        _uiState.update { state ->
            state.copy(selectedProblemType = problemType).withFilteredProblems()
        }
    }

    /** Changes ordering between curriculum order, Easy first, and Hard first. */
    fun selectSortOption(option: ProblemSortOption) {
        if (_uiState.value.sortOption == option) return
        _uiState.update { state ->
            state.copy(sortOption = option).withFilteredProblems()
        }
    }

    /** Reloads repository data after an error. */
    fun retry() = loadProblems()

    /** Collects problems once and derives the available topic filters from the loaded dataset. */
    private fun loadProblems() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getProblems().collect { result ->
                _uiState.update { state ->
                    when (result) {
                        Resource.Loading -> state.copy(isLoading = true, errorMessage = null)
                        is Resource.Success -> state.copy(
                            allProblems = result.data,
                            availableProblemTypes = ProblemTypeCatalog.filtersFor(result.data),
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

    /**
     * Produces the visible list after applying all active filters.
     *
     * Search includes curriculum labels so users can type "loop" or "syntax" even if the raw MBPP
     * task title does not contain that exact word.
     */
    private fun ProblemListUiState.withFilteredProblems(): ProblemListUiState {
        val query = searchQuery.trim()
        val filtered = allProblems
            .asSequence()
            .filter { problem ->
                selectedDifficulty == null || problem.difficulty == selectedDifficulty
            }
            .filter { problem ->
                ProblemTypeCatalog.matches(problem, selectedProblemType)
            }
            .filter { problem ->
                query.isBlank() ||
                    problem.title.contains(query, ignoreCase = true) ||
                    problem.description.contains(query, ignoreCase = true) ||
                    problem.tags.any { tag -> tag.contains(query, ignoreCase = true) } ||
                    ProblemTypeCatalog.matchesSearch(problem, query) ||
                    problem.order.toString() == query.removePrefix("#")
            }
            .toList()
            .sortedWith(sortOption.comparator)

        return copy(problems = filtered)
    }

    /** Maps UI sort options to the shared curriculum comparator. */
    private val ProblemSortOption.comparator: Comparator<Problem>
        get() = when (this) {
            ProblemSortOption.ORDER -> PythonProblemCurriculum.comparator()
            ProblemSortOption.EASY_FIRST -> compareBy<Problem> { problem ->
                problem.difficulty.rank
            }.then(PythonProblemCurriculum.comparator())
            ProblemSortOption.HARD_FIRST -> compareByDescending<Problem> { problem ->
                problem.difficulty.rank
            }.then(PythonProblemCurriculum.comparator())
        }

    private val Difficulty.rank: Int
        get() = when (this) {
            Difficulty.EASY -> 0
            Difficulty.MEDIUM -> 1
            Difficulty.HARD -> 2
        }
}
