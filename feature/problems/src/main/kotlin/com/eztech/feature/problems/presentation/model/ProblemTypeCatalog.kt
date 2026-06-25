package com.eztech.feature.problems.presentation.model

import com.eztech.core.domain.model.Problem

data class ProblemTypeFilter(
    val key: String,
    val label: String,
    val count: Int,
) {
    val displayLabel: String
        get() = "$label ($count)"
}

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

    fun filtersFor(problems: List<Problem>): List<ProblemTypeFilter> {
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
        return filters + generalFilter
    }

    fun labelsFor(problem: Problem): List<String> {
        val labels = definitions
            .filter { definition -> problem.matchesDefinition(definition) }
            .map(ProblemTypeDefinition::label)
        return labels.ifEmpty { listOf("General") }
    }

    fun matches(problem: Problem, selectedTypeKey: String?): Boolean =
        when (selectedTypeKey) {
            null -> true
            GENERAL_KEY -> isGeneralProblem(problem)
            else -> definitions
                .firstOrNull { definition -> definition.key == selectedTypeKey }
                ?.let { definition -> problem.matchesDefinition(definition) }
                ?: true
        }

    fun matchesSearch(problem: Problem, query: String): Boolean {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) return true

        val problemDefinitions = definitions.filter { definition ->
            problem.matchesDefinition(definition)
        }
        val searchValues = mutableListOf<String>().apply {
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

    private fun isGeneralProblem(problem: Problem): Boolean =
        problem.tags.map(String::lowercase).none { tag -> tag in specificTags }

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
