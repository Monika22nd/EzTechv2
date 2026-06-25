package com.eztech.core.domain.usecase.recommendation

import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.Recommendation
import com.eztech.core.domain.model.RecommendationType
import com.eztech.core.domain.model.User

class RecommendationEngine {
    fun generateRecommendations(
        user: User,
        problems: List<Problem>,
        lessons: List<Lesson>,
        maxResults: Int = DEFAULT_MAX_RESULTS,
    ): List<Recommendation> {
        if (maxResults <= 0 || problems.isEmpty()) return emptyList()

        val solvedProblemIds = user.solvedProblemIds.toSet()
        val watchedLessonIds = user.watchedLessonIds.toSet()
        val solvedProblems = problems.filter { problem -> problem.id in solvedProblemIds }
        val unsolvedProblems = problems
            .filterNot { problem -> problem.id in solvedProblemIds }
            .sortedProblemsByLearningOrder()
        val candidates = mutableListOf<Recommendation>()

        candidates += learningPathRecommendations(unsolvedProblems)
        candidates += adaptiveDifficultyRecommendations(
            solvedProblems = solvedProblems,
            unsolvedProblems = unsolvedProblems,
        )
        candidates += problemTypeRecommendations(
            solvedProblems = solvedProblems,
            unsolvedProblems = unsolvedProblems,
        )

        val focusTags = candidates
            .flatMap(Recommendation::tags)
            .map { tag -> tag.normalizeTag() }
            .filter { tag -> tag in problemTypeDefinitions }
            .distinct()
        candidates += lessonRecommendations(
            focusTags = focusTags,
            lessons = lessons,
            watchedLessonIds = watchedLessonIds,
        )

        return candidates
            .sortedByDescending(Recommendation::score)
            .distinctBy { recommendation -> recommendation.targetKey }
            .take(maxResults)
    }

    private fun learningPathRecommendations(
        unsolvedProblems: List<Problem>,
    ): List<Recommendation> = unsolvedProblems
        .take(3)
        .mapIndexed { index, problem ->
            problem.toRecommendation(
                idPrefix = "path",
                reason = "Continue with the next problem in your Python path.",
                score = 0.72f - index * 0.03f,
            )
        }

    private fun adaptiveDifficultyRecommendations(
        solvedProblems: List<Problem>,
        unsolvedProblems: List<Problem>,
    ): List<Recommendation> {
        val easySolved = solvedProblems.count { problem -> problem.difficulty == Difficulty.EASY }
        val mediumSolved = solvedProblems.count { problem -> problem.difficulty == Difficulty.MEDIUM }
        val targetDifficulty = when {
            mediumSolved >= MEDIUM_TO_HARD_THRESHOLD -> Difficulty.HARD
            easySolved >= EASY_TO_MEDIUM_THRESHOLD -> Difficulty.MEDIUM
            else -> Difficulty.EASY
        }
        val reason = when (targetDifficulty) {
            Difficulty.EASY -> if (solvedProblems.isEmpty()) {
                "You are just starting out, so this Easy problem builds the basics first."
            } else {
                "You have solved ${solvedProblems.size} problem(s), so another Easy problem helps reinforce fundamentals."
            }
            Difficulty.MEDIUM -> "You solved $easySolved Easy problem(s), so the system is moving you up to Medium."
            Difficulty.HARD -> "You solved $mediumSolved Medium problem(s), so this Hard challenge is the next step."
        }

        return unsolvedProblems
            .filter { problem -> problem.difficulty == targetDifficulty }
            .take(2)
            .mapIndexed { index, problem ->
                val baseScore = when (targetDifficulty) {
                    Difficulty.EASY -> if (solvedProblems.isEmpty()) 0.96f else 0.67f
                    Difficulty.MEDIUM,
                    Difficulty.HARD -> 0.94f
                }
                problem.toRecommendation(
                    idPrefix = "difficulty",
                    reason = reason,
                    score = baseScore - index * 0.04f,
                )
            }
    }

    private fun problemTypeRecommendations(
        solvedProblems: List<Problem>,
        unsolvedProblems: List<Problem>,
    ): List<Recommendation> {
        val solvedCountByTag = problemTypeDefinitions.keys.associateWith { tag ->
            solvedProblems.count { problem -> tag in problem.normalizedTags }
        }
        val weakTags = problemTypeDefinitions.keys
            .filter { tag -> unsolvedProblems.any { problem -> tag in problem.normalizedTags } }
            .sortedWith(
                compareBy<String> { tag -> solvedCountByTag[tag] ?: 0 }
                    .thenBy { tag -> problemTypeDefinitions[tag].orEmpty() },
            )
            .take(3)

        return weakTags.mapIndexedNotNull { index, tag ->
            val problem = unsolvedProblems
                .filter { candidate -> tag in candidate.normalizedTags }
                .sortedWith(compareBy<Problem> { candidate ->
                    candidate.difficulty.rank
                }.thenBy { candidate ->
                    candidate.order.takeIf { it > 0 } ?: Int.MAX_VALUE
                }.thenBy { candidate ->
                    candidate.title
                })
                .firstOrNull()
                ?: return@mapIndexedNotNull null
            val solvedCount = solvedCountByTag[tag] ?: 0
            val label = problemTypeDefinitions[tag].orEmpty()
            problem.toRecommendation(
                idPrefix = "type_$tag",
                reason = if (solvedCount == 0) {
                    "You have solved 0 $label problem(s), so this fills a missing practice area."
                } else {
                    "You have solved only $solvedCount $label problem(s), so this adds focused practice."
                },
                score = 0.9f - index * 0.05f - solvedCount.coerceAtMost(8) * 0.01f,
                preferredTag = tag,
            )
        }
    }

    private fun lessonRecommendations(
        focusTags: List<String>,
        lessons: List<Lesson>,
        watchedLessonIds: Set<String>,
    ): List<Recommendation> {
        if (focusTags.isEmpty()) return emptyList()
        return focusTags.mapIndexedNotNull { index, tag ->
            val lesson = lessons
                .filterNot { lesson -> lesson.id in watchedLessonIds || lesson.watched }
                .sortedLessonsByLearningOrder()
                .firstOrNull { lesson -> lesson.matchesTag(tag) }
                ?: return@mapIndexedNotNull null
            val label = problemTypeDefinitions[tag].orEmpty()
            Recommendation(
                id = "lesson_${lesson.id}",
                type = RecommendationType.LESSON,
                title = lesson.title,
                subtitle = lesson.type.name.lowercase().replaceFirstChar(Char::uppercase),
                reason = "Your current recommendations focus on $label, so review this lesson before practicing.",
                score = 0.64f - index * 0.03f,
                lesson = lesson,
                tags = listOf(tag),
            )
        }
    }

    private fun Problem.toRecommendation(
        idPrefix: String,
        reason: String,
        score: Float,
        preferredTag: String? = null,
    ): Recommendation {
        val displayTags = if (preferredTag != null) {
            listOf(preferredTag)
        } else {
            normalizedTags.filter { tag -> tag in problemTypeDefinitions }.take(2)
        }
        return Recommendation(
            id = "${idPrefix}_$id",
            type = RecommendationType.PROBLEM,
            title = title,
            subtitle = listOfNotNull(
                difficulty.name.lowercase().replaceFirstChar(Char::uppercase),
                displayTags.firstOrNull()?.let { tag -> problemTypeDefinitions[tag] },
            ).joinToString(" - "),
            reason = reason,
            score = score,
            problem = this,
            tags = displayTags,
        )
    }

    private fun Lesson.matchesTag(tag: String): Boolean {
        val haystack = "$categoryId $title $description $content".lowercase()
        val aliases = tagAliases[tag].orEmpty() + tag + problemTypeDefinitions[tag].orEmpty()
        return aliases.any { alias -> haystack.contains(alias.lowercase()) }
    }

    private val Recommendation.targetKey: String
        get() = when (type) {
            RecommendationType.PROBLEM -> "problem:${problem?.id.orEmpty()}"
            RecommendationType.LESSON -> "lesson:${lesson?.id.orEmpty()}"
        }

    private val Problem.normalizedTags: Set<String>
        get() = tags.map { tag -> tag.normalizeTag() }.toSet()

    private fun List<Problem>.sortedProblemsByLearningOrder(): List<Problem> =
        sortedWith(compareBy<Problem> { problem ->
            problem.order.takeIf { it > 0 } ?: Int.MAX_VALUE
        }.thenBy { problem -> problem.title })

    private fun List<Lesson>.sortedLessonsByLearningOrder(): List<Lesson> =
        sortedWith(compareBy<Lesson> { lesson ->
            lesson.order.takeIf { it > 0 } ?: Int.MAX_VALUE
        }.thenBy { lesson -> lesson.title })

    private val Difficulty.rank: Int
        get() = when (this) {
            Difficulty.EASY -> 0
            Difficulty.MEDIUM -> 1
            Difficulty.HARD -> 2
        }

    private fun String.normalizeTag(): String =
        trim().lowercase()

    private companion object {
        const val DEFAULT_MAX_RESULTS = 5
        const val EASY_TO_MEDIUM_THRESHOLD = 5
        const val MEDIUM_TO_HARD_THRESHOLD = 5

        val problemTypeDefinitions = linkedMapOf(
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
        )

        val tagAliases = mapOf(
            "lists" to listOf("list", "array", "sequence"),
            "strings" to listOf("string", "text"),
            "math" to listOf("math", "number", "integer"),
            "tuples" to listOf("tuple"),
            "sorting" to listOf("sort", "order"),
            "sets" to listOf("set", "unique"),
            "dictionaries" to listOf("dict", "dictionary", "map"),
            "regex" to listOf("regex", "regular expression"),
            "heap" to listOf("heap", "priority queue"),
            "binary" to listOf("binary", "bit", "bitwise"),
            "date-time" to listOf("date", "time"),
            "matrix" to listOf("matrix", "grid"),
            "recursion" to listOf("recursion", "recursive"),
            "trees" to listOf("tree", "graph"),
        )
    }
}
