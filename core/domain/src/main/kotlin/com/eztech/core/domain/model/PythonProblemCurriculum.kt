package com.eztech.core.domain.model

/**
 * Shared curriculum classifier for Python practice problems.
 *
 * Firestore/MBPP data can arrive in a raw dataset order that is not beginner friendly. This object
 * normalizes ordering into learning stages so Problems, Dashboard, and Recommendations all agree on
 * the same progression: syntax first, then operators, conditionals, loops, strings, lists, etc.
 */
object PythonProblemCurriculum {
    /** Ordered learning stages displayed as filters and used for recommendation ranking. */
    val stages: List<PythonProblemStage> = listOf(
        PythonProblemStage(
            key = "syntax",
            label = "Syntax basics",
            description = "Functions, return values, simple expressions, and Python syntax.",
            order = 1,
        ),
        PythonProblemStage(
            key = "operators",
            label = "Variables and operators",
            description = "Numbers, arithmetic, comparisons, and small formulas.",
            order = 2,
        ),
        PythonProblemStage(
            key = "conditionals",
            label = "Conditionals",
            description = "if/else decisions and boolean checks.",
            order = 3,
        ),
        PythonProblemStage(
            key = "loops",
            label = "For and while loops",
            description = "Repeated work with for loops, while loops, and counters.",
            order = 4,
        ),
        PythonProblemStage(
            key = "strings",
            label = "Strings",
            description = "Text processing, slicing, and character checks.",
            order = 5,
        ),
        PythonProblemStage(
            key = "lists",
            label = "Lists",
            description = "Arrays, list traversal, indexing, and nested lists.",
            order = 6,
        ),
        PythonProblemStage(
            key = "collections",
            label = "Tuples, sets, and dictionaries",
            description = "Python collections beyond lists.",
            order = 7,
        ),
        PythonProblemStage(
            key = "functions",
            label = "Functions practice",
            description = "More function-focused practice after the basics.",
            order = 8,
        ),
        PythonProblemStage(
            key = "algorithms",
            label = "Algorithms",
            description = "Sorting, recursion, binary logic, heaps, matrices, trees, and regex.",
            order = 9,
        ),
    )

    /**
     * Classifies a problem into the earliest matching curriculum stage.
     *
     * Tags are preferred when present, while title/description/starter/solution text are used as a
     * fallback so imported datasets still sort correctly even when tags are incomplete.
     */
    fun stageFor(problem: Problem): PythonProblemStage {
        val tags = problem.tags.map(String::lowercase).toSet()
        val text = problem.searchText()
        return when {
            tags.any { it in algorithmTags } || text.containsAny(algorithmWords) ->
                stage("algorithms")
            tags.any { it in collectionTags } || text.containsAny(collectionWords) ->
                stage("collections")
            "lists" in tags || text.containsAny(listWords) ->
                stage("lists")
            "strings" in tags || text.containsAny(stringWords) ->
                stage("strings")
            text.containsAny(loopWords) ->
                stage("loops")
            text.containsAny(conditionWords) ->
                stage("conditionals")
            "math" in tags || text.containsAny(operatorWords) ->
                stage("operators")
            "functions" in tags ->
                stage("syntax")
            else ->
                stage("syntax")
        }
    }

    /** Comparator used by lists and dashboards to show easier curriculum stages before later ones. */
    fun comparator(): Comparator<Problem> {
        val stageCache = mutableMapOf<String, PythonProblemStage>()

        fun cachedStage(problem: Problem): PythonProblemStage =
            stageCache.getOrPut(problem.id) { stageFor(problem) }

        return compareBy<Problem> { problem -> cachedStage(problem).order }
            .thenBy { problem -> problem.difficulty.rank }
            .thenBy { problem -> problem.order.takeIf { it > 0 } ?: Int.MAX_VALUE }
            .thenBy { problem -> problem.title }
    }

    /** Convenience wrapper for applying the shared curriculum comparator. */
    fun sorted(problems: List<Problem>): List<Problem> =
        problems.sortedWith(comparator())

    /** Counts solved problems in one curriculum stage for recommendation stats. */
    fun solvedCountForStage(
        problems: List<Problem>,
        solvedProblemIds: Set<String>,
        stage: PythonProblemStage,
    ): Int = problems.count { problem ->
        problem.id in solvedProblemIds && stageFor(problem).key == stage.key
    }

    /** Counts all available problems in one curriculum stage. */
    fun totalCountForStage(
        problems: List<Problem>,
        stage: PythonProblemStage,
    ): Int = problems.count { problem -> stageFor(problem).key == stage.key }

    /**
     * Finds the first stage that still has unsolved problems.
     *
     * This is the user's current learning stage; later logic uses it to avoid jumping directly from
     * syntax to data structures before earlier practice has been completed.
     */
    fun nextStageFor(
        problems: List<Problem>,
        solvedProblemIds: Set<String>,
    ): PythonProblemStage = stages.firstOrNull { stage ->
        problems.any { problem ->
            problem.id !in solvedProblemIds && stageFor(problem).key == stage.key
        }
    } ?: stages.last()

    /** Converts a raw tag key into a readable label for chips and explanations. */
    fun labelForTag(tag: String): String =
        tagLabels[tag.lowercase()] ?: tag.replaceFirstChar(Char::uppercase)

    /** Looks up a stage by key; keys are controlled by the local stages list. */
    private fun stage(key: String): PythonProblemStage =
        stages.first { stage -> stage.key == key }

    /** Builds a searchable text blob from all fields that may reveal the topic of a problem. */
    private fun Problem.searchText(): String =
        "$title\n$description\n$starterCode\n$solutionCode\n${tags.joinToString(" ")}"
            .lowercase()

    /** Returns true when any topic keyword appears in the text blob. */
    private fun String.containsAny(values: Iterable<String>): Boolean =
        values.any(::contains)

    private val Difficulty.rank: Int
        get() = when (this) {
            Difficulty.EASY -> 0
            Difficulty.MEDIUM -> 1
            Difficulty.HARD -> 2
        }

    private val algorithmTags = setOf(
        "sorting",
        "regex",
        "heap",
        "binary",
        "date-time",
        "matrix",
        "recursion",
        "trees",
        "graphs",
    )
    private val collectionTags = setOf("tuples", "sets", "dictionaries")

    private val algorithmWords = listOf(
        "sort",
        "sorted",
        "regular expression",
        "regex",
        "heap",
        "binary",
        "bitwise",
        "matrix",
        "recursion",
        "recursive",
        "tree",
        "graph",
    )
    private val collectionWords = listOf("tuple", "dictionary", "dict", "set")
    private val listWords = listOf("list", "array", "sequence", "elements")
    private val stringWords = listOf("string", "character", "substring", "word", "text")
    private val loopWords = listOf("for ", "while ", "range(", "iterate", "loop", "multiples")
    private val conditionWords = listOf(
        " if ",
        "\nif ",
        "check",
        "whether",
        "valid",
        "prime",
        "true",
        "false",
    )
    private val operatorWords = listOf(
        "number",
        "integer",
        "sum",
        "product",
        "area",
        "volume",
        "perimeter",
        "calculate",
        "arithmetic",
    )

    private val tagLabels = mapOf(
        "syntax" to "Syntax",
        "operators" to "Operators",
        "conditionals" to "Conditionals",
        "loops" to "Loops",
        "functions" to "Functions",
        "lists" to "Lists",
        "strings" to "Strings",
        "math" to "Math",
        "tuples" to "Tuples",
        "sorting" to "Sorting",
        "sets" to "Sets",
        "dictionaries" to "Dictionaries",
        "regex" to "Regex",
        "heap" to "Heap",
        "binary" to "Binary",
        "date-time" to "Date/time",
        "matrix" to "Matrix",
        "recursion" to "Recursion",
        "trees" to "Trees",
        "graphs" to "Graphs",
    )
}

/** One ordered stage in the Python practice curriculum. */
data class PythonProblemStage(
    val key: String,
    val label: String,
    val description: String,
    val order: Int,
)
