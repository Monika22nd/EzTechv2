package com.eztech.feature.problems.presentation.model

import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.PythonProblemCurriculum

data class ProblemTypeFilter(
    val key: String,
    val label: String,
    val count: Int,
) {
    /** Text shown in filter chips, e.g. "Loops (42)". */
    val displayLabel: String
        get() = "$label ($count)"
}

/**
 * Creates and matches problem topic filters.
 *
 * Curriculum stages are placed before raw MBPP tags so the first filters follow a teaching order
 * instead of a dataset order. Detailed tags remain available for more specific searching.
 */
object ProblemTypeCatalog {
    private const val GENERAL_KEY = "general"

    private val definitions = listOf(
        ProblemTypeDefinition(
            key = "lists",
            label = "Lists",
            tags = setOf("lists"),
            aliases = setOf("array", "arrays", "list"),
        ),
        ProblemTypeDefinition(
            key = "strings",
            label = "Strings",
            tags = setOf("strings"),
            aliases = setOf("string", "text"),
        ),
        ProblemTypeDefinition(
            key = "math",
            label = "Math",
            tags = setOf("math"),
            aliases = setOf("number", "integer", "prime"),
        ),
        ProblemTypeDefinition(
            key = "tuples",
            label = "Tuples",
            tags = setOf("tuples"),
            aliases = setOf("tuple"),
        ),
        ProblemTypeDefinition(
            key = "sorting",
            label = "Sorting",
            tags = setOf("sorting"),
            aliases = setOf("sort", "order"),
        ),
        ProblemTypeDefinition(
            key = "sets",
            label = "Sets",
            tags = setOf("sets"),
            aliases = setOf("set", "unique"),
        ),
        ProblemTypeDefinition(
            key = "dictionaries",
            label = "Dictionaries",
            tags = setOf("dictionaries"),
            aliases = setOf("dict", "map", "hash map"),
        ),
        ProblemTypeDefinition(
            key = "regex",
            label = "Regex",
            tags = setOf("regex"),
            aliases = setOf("regular expression"),
        ),
        ProblemTypeDefinition(
            key = "heap",
            label = "Heap",
            tags = setOf("heap"),
            aliases = setOf("priority queue"),
        ),
        ProblemTypeDefinition(
            key = "binary",
            label = "Binary",
            tags = setOf("binary"),
            aliases = setOf("bit", "bitwise"),
        ),
        ProblemTypeDefinition(
            key = "date-time",
            label = "Date/time",
            tags = setOf("date-time"),
            aliases = setOf("date", "time"),
        ),
        ProblemTypeDefinition(
            key = "matrix",
            label = "Matrix",
            tags = setOf("matrix"),
            aliases = setOf("grid", "2d array"),
        ),
        ProblemTypeDefinition(
            key = "recursion",
            label = "Recursion",
            tags = setOf("recursion"),
            aliases = setOf("recursive"),
        ),
        ProblemTypeDefinition(
            key = "trees",
            label = "Trees",
            tags = setOf("trees", "graphs"),
            aliases = setOf("tree", "graph"),
        ),
    )

    private val specificTags = definitions.flatMap(ProblemTypeDefinition::tags).toSet()

    /** Builds visible filter chips from the currently loaded problem set. */
    fun filtersFor(problems: List<Problem>): List<ProblemTypeFilter> {
        val stageFilters = PythonProblemCurriculum.stages.mapNotNull { stage ->
            val count = problems.count { problem ->
                PythonProblemCurriculum.stageFor(problem).key == stage.key
            }
            if (count == 0) return@mapNotNull null
            ProblemTypeFilter(
                key = stage.key,
                label = stage.label,
                count = count,
            )
        }
        val filters = definitions.mapNotNull { definition ->
            val count = problems.count { problem -> problem.matchesDefinition(definition) }
            if (count == 0) return@mapNotNull null
            ProblemTypeFilter(
                key = definition.key,
                label = definition.label,
                count = count,
            )
        }
        val generalCount = problems.count { problem -> isGeneralProblem(problem) }
        val generalFilter = if (generalCount > 0) {
            listOf(
                ProblemTypeFilter(
                    key = GENERAL_KEY,
                    label = "General",
                    count = generalCount,
                ),
            )
        } else {
            emptyList()
        }
        return stageFilters + filters.filterNot { filter ->
            stageFilters.any { stageFilter -> stageFilter.key == filter.key }
        } + generalFilter
    }

    /** Returns labels shown on a problem card, always starting with the curriculum stage. */
    fun labelsFor(problem: Problem): List<String> {
        val stage = PythonProblemCurriculum.stageFor(problem)
        val labels = definitions
            .filter { definition -> problem.matchesDefinition(definition) }
            .map(ProblemTypeDefinition::label)
            .filterNot { label -> label == stage.label }
        return (listOf(stage.label) + labels).distinct().ifEmpty { listOf("General") }
    }

    /** Checks whether a problem belongs to the selected filter chip. */
    fun matches(problem: Problem, selectedTypeKey: String?): Boolean =
        when (selectedTypeKey) {
            null -> true
            GENERAL_KEY -> isGeneralProblem(problem)
            in PythonProblemCurriculum.stages.map { stage -> stage.key } ->
                PythonProblemCurriculum.stageFor(problem).key == selectedTypeKey
            else -> definitions
                .firstOrNull { definition -> definition.key == selectedTypeKey }
                ?.let { definition -> problem.matchesDefinition(definition) }
                ?: true
        }

    /** Adds topic-aware search beyond plain title/description matching. */
    fun matchesSearch(problem: Problem, query: String): Boolean {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) return true

        val problemDefinitions = definitions.filter { definition ->
            problem.matchesDefinition(definition)
        }
        val stage = PythonProblemCurriculum.stageFor(problem)
        val searchValues = mutableListOf<String>().apply {
            add(stage.key)
            add(stage.label)
            add(stage.description)
            problemDefinitions.forEach { definition ->
                add(definition.label)
                add(definition.key)
                addAll(definition.tags)
                addAll(definition.aliases)
            }
            if (isGeneralProblem(problem)) {
                add("General")
                add(GENERAL_KEY)
                add("basic")
            }
        }
        return searchValues.any { value ->
            value.contains(normalizedQuery, ignoreCase = true)
        }
    }

    /** Problems with no known specific tag are grouped under the General filter. */
    private fun isGeneralProblem(problem: Problem): Boolean =
        problem.tags.map(String::lowercase).none { tag -> tag in specificTags }

    /** Matches one raw tag definition against a problem's imported tags. */
    private fun Problem.matchesDefinition(definition: ProblemTypeDefinition): Boolean {
        val normalizedTags = tags.map(String::lowercase).toSet()
        return normalizedTags.any { tag -> tag in definition.tags }
    }

    private data class ProblemTypeDefinition(
        val key: String,
        val label: String,
        val tags: Set<String>,
        val aliases: Set<String> = emptySet(),
    )
}
