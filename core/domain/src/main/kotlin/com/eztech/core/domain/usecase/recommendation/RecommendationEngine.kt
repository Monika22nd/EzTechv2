package com.eztech.core.domain.usecase.recommendation

import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.PythonProblemCurriculum
import com.eztech.core.domain.model.Recommendation
import com.eztech.core.domain.model.RecommendationDashboard
import com.eztech.core.domain.model.RecommendationMetric
import com.eztech.core.domain.model.RecommendationStats
import com.eztech.core.domain.model.RecommendationType
import com.eztech.core.domain.model.User

/**
 * Pure Kotlin recommendation engine for lessons and problems.
 *
 * The engine does not call Firebase or Android APIs. It receives already-loaded domain models,
 * calculates transparent learning statistics, then returns ranked recommendations with explanation
 * text and metric chips. Keeping it pure makes it easy to unit test and safe to reuse from multiple
 * screens.
 */
class RecommendationEngine {
    /**
     * Builds the full recommendation dashboard for one user.
     *
     * The algorithm first orders problems through PythonProblemCurriculum, then combines three
     * recommendation sources: next path step, adaptive difficulty, and weak curriculum area. Matching
     * lessons are added afterward so the user can review theory before solving.
     */
    fun generateDashboard(
        user: User,
        problems: List<Problem>,
        lessons: List<Lesson>,
        maxResults: Int = DEFAULT_MAX_RESULTS,
    ): RecommendationDashboard {
        if (maxResults <= 0 || problems.isEmpty()) {
            return RecommendationDashboard(
                stats = emptyStats(),
                recommendations = emptyList(),
            )
        }

        val solvedProblemIds = user.solvedProblemIds.toSet()
        val watchedLessonIds = user.watchedLessonIds.toSet()
        val sortedProblems = PythonProblemCurriculum.sorted(problems)
        val solvedProblems = sortedProblems.filter { problem -> problem.id in solvedProblemIds }
        val unsolvedProblems = sortedProblems.filterNot { problem -> problem.id in solvedProblemIds }
        val stats = buildStats(
            problems = sortedProblems,
            solvedProblems = solvedProblems,
            solvedProblemIds = solvedProblemIds,
        )
        val candidates = mutableListOf<Recommendation>()

        candidates += learningPathRecommendations(
            stats = stats,
            problems = sortedProblems,
            solvedProblemIds = solvedProblemIds,
            unsolvedProblems = unsolvedProblems,
        )
        candidates += adaptiveDifficultyRecommendations(
            stats = stats,
            solvedProblems = solvedProblems,
            unsolvedProblems = unsolvedProblems,
        )
        candidates += weakStageRecommendations(
            stats = stats,
            problems = sortedProblems,
            solvedProblemIds = solvedProblemIds,
            unsolvedProblems = unsolvedProblems,
        )

        val focusTags = candidates
            .flatMap(Recommendation::tags)
            .map { tag -> tag.normalizeTag() }
            .distinct()
        candidates += lessonRecommendations(
            stats = stats,
            focusTags = focusTags,
            lessons = lessons,
            watchedLessonIds = watchedLessonIds,
        )

        return RecommendationDashboard(
            stats = stats,
            recommendations = candidates
                .distinctBy { recommendation -> recommendation.targetKey }
                .sortedWith(recommendationComparator())
                .take(maxResults),
        )
    }

    /**
     * Compatibility helper for call sites/tests that only need cards and not the stats panel.
     */
    fun generateRecommendations(
        user: User,
        problems: List<Problem>,
        lessons: List<Lesson>,
        maxResults: Int = DEFAULT_MAX_RESULTS,
    ): List<Recommendation> =
        generateDashboard(
            user = user,
            problems = problems,
            lessons = lessons,
            maxResults = maxResults,
        ).recommendations

    /**
     * Calculates the learning signals shown to the user and reused by scoring.
     *
     * The current stage is the first curriculum stage with unsolved work, while weakestArea is the
     * stage with the smallest solved count. Difficulty gates are based on solved Easy/Medium totals.
     */
    private fun buildStats(
        problems: List<Problem>,
        solvedProblems: List<Problem>,
        solvedProblemIds: Set<String>,
    ): RecommendationStats {
        val currentStage = PythonProblemCurriculum.nextStageFor(
            problems = problems,
            solvedProblemIds = solvedProblemIds,
        )
        val weakestStage = PythonProblemCurriculum.stages
            .filter { stage -> PythonProblemCurriculum.totalCountForStage(problems, stage) > 0 }
            .minWithOrNull(
                compareBy<com.eztech.core.domain.model.PythonProblemStage> { stage ->
                    PythonProblemCurriculum.solvedCountForStage(
                        problems = problems,
                        solvedProblemIds = solvedProblemIds,
                        stage = stage,
                    )
                }.thenBy { stage -> stage.order },
            ) ?: currentStage

        return RecommendationStats(
            solvedProblems = solvedProblems.size,
            totalProblems = problems.size,
            solvedEasy = solvedProblems.count { problem -> problem.difficulty == Difficulty.EASY },
            solvedMedium = solvedProblems.count { problem -> problem.difficulty == Difficulty.MEDIUM },
            solvedHard = solvedProblems.count { problem -> problem.difficulty == Difficulty.HARD },
            currentStage = currentStage.label,
            nextStage = nextUnmasteredStageLabel(
                problems = problems,
                solvedProblemIds = solvedProblemIds,
                currentStageOrder = currentStage.order,
            ),
            nextDifficulty = targetDifficulty(solvedProblems, currentStage.label),
            weakestArea = weakestStage.label,
            stageSolved = PythonProblemCurriculum.solvedCountForStage(
                problems = problems,
                solvedProblemIds = solvedProblemIds,
                stage = currentStage,
            ),
            stageTotal = PythonProblemCurriculum.totalCountForStage(problems, currentStage),
        )
    }

    /**
     * Recommends the next curriculum-ordered problems.
     *
     * This is the strongest signal because it keeps beginners on syntax/operators/loops before more
     * complex strings, lists, collections, and algorithms.
     */
    private fun learningPathRecommendations(
        stats: RecommendationStats,
        problems: List<Problem>,
        solvedProblemIds: Set<String>,
        unsolvedProblems: List<Problem>,
    ): List<Recommendation> = unsolvedProblems
        .take(LEARNING_PATH_CANDIDATES)
        .mapIndexed { index, problem ->
            val stage = PythonProblemCurriculum.stageFor(problem)
            val stageSolved = PythonProblemCurriculum.solvedCountForStage(
                problems = problems,
                solvedProblemIds = solvedProblemIds,
                stage = stage,
            )
            val stageTotal = PythonProblemCurriculum.totalCountForStage(
                problems = problems,
                stage = stage,
            )
            val reason = when {
                stats.solvedProblems == 0 ->
                    "You are just starting out, so the path begins with ${stage.label} before data structures."
                problem.difficulty == Difficulty.MEDIUM && stats.nextDifficulty == Difficulty.MEDIUM ->
                    "You solved ${stats.solvedEasy} Easy problem(s), so this Medium problem is the next step."
                problem.difficulty == Difficulty.HARD && stats.nextDifficulty == Difficulty.HARD ->
                    "You solved ${stats.solvedMedium} Medium problem(s), so this Hard problem is the next step."
                stageSolved == 0 ->
                    "You have solved 0 ${stage.label} problem(s), so this fills a missing practice area."
                stage.label == stats.currentStage ->
                    "Your current stage is ${stats.currentStage}. Finish these basics before moving to ${stats.nextStage}."
                else ->
                    "This is the next step after ${stats.currentStage}, ordered from easier Python concepts to harder ones."
            }
            problem.toRecommendation(
                idPrefix = "path",
                reason = reason,
                score = 0.98f - index * 0.02f,
                preferredTag = stage.key,
                sequenceLabel = "Step ${index + 1}",
                metrics = listOf(
                    RecommendationMetric("Stage solved", "$stageSolved/${stageTotal.coerceAtLeast(1)}"),
                    RecommendationMetric("Difficulty", problem.difficulty.displayName),
                    RecommendationMetric("Path", "${stage.order}/${PythonProblemCurriculum.stages.size}"),
                ),
            )
        }

    /**
     * Recommends problems at the target difficulty inferred from user progress.
     *
     * A new learner stays on Easy; after enough Easy solves the engine can introduce Medium, then
     * Hard after enough Medium solves.
     */
    private fun adaptiveDifficultyRecommendations(
        stats: RecommendationStats,
        solvedProblems: List<Problem>,
        unsolvedProblems: List<Problem>,
    ): List<Recommendation> {
        val targetDifficulty = stats.nextDifficulty
        val currentStageProblems = unsolvedProblems.filter { problem ->
            PythonProblemCurriculum.stageFor(problem).label == stats.currentStage &&
                problem.difficulty == targetDifficulty
        }
        val fallbackProblems = unsolvedProblems.filter { problem ->
            problem.difficulty == targetDifficulty
        }
        val reason = when (targetDifficulty) {
            Difficulty.EASY -> if (solvedProblems.isEmpty()) {
                "The system keeps you on Easy because no problems are solved yet."
            } else {
                "You have solved ${stats.solvedProblems} problem(s), but this stage still needs Easy practice."
            }
            Difficulty.MEDIUM ->
                "You solved ${stats.solvedEasy} Easy problem(s), so the next recommendation can move to Medium."
            Difficulty.HARD ->
                "You solved ${stats.solvedMedium} Medium problem(s), so the next recommendation can move to Hard."
        }

        return (currentStageProblems.ifEmpty { fallbackProblems })
            .take(2)
            .mapIndexed { index, problem ->
                problem.toRecommendation(
                    idPrefix = "difficulty",
                    reason = reason,
                    score = 0.86f - index * 0.03f,
                    preferredTag = PythonProblemCurriculum.stageFor(problem).key,
                    sequenceLabel = "Level up",
                    metrics = listOf(
                        RecommendationMetric("Easy", stats.solvedEasy.toString()),
                        RecommendationMetric("Medium", stats.solvedMedium.toString()),
                        RecommendationMetric("Hard", stats.solvedHard.toString()),
                    ),
                )
            }
    }

    /**
     * Recommends practice from stages where the user has the least solved work.
     *
     * This prevents the path from becoming one-dimensional, for example solving many list problems
     * while never touching loops or conditionals.
     */
    private fun weakStageRecommendations(
        stats: RecommendationStats,
        problems: List<Problem>,
        solvedProblemIds: Set<String>,
        unsolvedProblems: List<Problem>,
    ): List<Recommendation> {
        val weakStages = PythonProblemCurriculum.stages
            .filter { stage -> PythonProblemCurriculum.totalCountForStage(problems, stage) > 0 }
            .sortedWith(
                compareBy<com.eztech.core.domain.model.PythonProblemStage> { stage ->
                    PythonProblemCurriculum.solvedCountForStage(
                        problems = problems,
                        solvedProblemIds = solvedProblemIds,
                        stage = stage,
                    )
                }.thenBy { stage -> stage.order },
            )
            .take(3)

        return weakStages.mapIndexedNotNull { index, stage ->
            val problem = unsolvedProblems.firstOrNull { candidate ->
                PythonProblemCurriculum.stageFor(candidate).key == stage.key
            } ?: return@mapIndexedNotNull null
            val solvedCount = PythonProblemCurriculum.solvedCountForStage(
                problems = problems,
                solvedProblemIds = solvedProblemIds,
                stage = stage,
            )
            val totalCount = PythonProblemCurriculum.totalCountForStage(problems, stage)
            problem.toRecommendation(
                idPrefix = "stage_${stage.key}",
                reason = if (solvedCount == 0) {
                    "You have solved 0 ${stage.label} problem(s), so this fills the weakest area first."
                } else {
                    "You have solved only $solvedCount/$totalCount ${stage.label} problem(s), so this keeps practice balanced."
                },
                score = 0.82f - index * 0.04f,
                preferredTag = stage.key,
                sequenceLabel = "Weak area",
                metrics = listOf(
                    RecommendationMetric("Weak area", stats.weakestArea),
                    RecommendationMetric("Solved here", "$solvedCount/$totalCount"),
                    RecommendationMetric("Difficulty", problem.difficulty.displayName),
                ),
            )
        }
    }

    /**
     * Recommends unwatched lessons that match tags from problem recommendations.
     *
     * Problem cards remain first in sorting, but lesson cards give a learner an immediate review
     * option when the recommended practice area feels unfamiliar.
     */
    private fun lessonRecommendations(
        stats: RecommendationStats,
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
            val label = PythonProblemCurriculum.labelForTag(tag)
            Recommendation(
                id = "lesson_${lesson.id}",
                type = RecommendationType.LESSON,
                title = lesson.title,
                subtitle = lesson.type.name.lowercase().replaceFirstChar(Char::uppercase),
                reason = "Your current stage is ${stats.currentStage}; review this $label lesson before practicing.",
                score = 0.62f - index * 0.03f,
                stageLabel = label,
                sequenceLabel = "Review",
                metrics = listOf(
                    RecommendationMetric("Current stage", stats.currentStage),
                    RecommendationMetric("Progress", "${stats.progressPercent}%"),
                ),
                lesson = lesson,
                tags = listOf(tag),
            )
        }
    }

    /** Converts a Problem into the shared card model used by Home and Recommendations screens. */
    private fun Problem.toRecommendation(
        idPrefix: String,
        reason: String,
        score: Float,
        preferredTag: String? = null,
        sequenceLabel: String,
        metrics: List<RecommendationMetric>,
    ): Recommendation {
        val stage = PythonProblemCurriculum.stageFor(this)
        val displayTags = if (preferredTag != null) {
            listOf(preferredTag)
        } else {
            normalizedTags.take(2)
        }
        return Recommendation(
            id = "${idPrefix}_$id",
            type = RecommendationType.PROBLEM,
            title = title,
            subtitle = "${stage.label} - ${difficulty.displayName}",
            reason = reason,
            score = score,
            stageLabel = stage.label,
            sequenceLabel = sequenceLabel,
            metrics = metrics,
            problem = this,
            tags = displayTags,
        )
    }

    /**
     * Determines the next difficulty level.
     *
     * Stage progress has priority so a user does not skip Easy practice inside a brand-new stage,
     * even if their overall solved count is already high.
     */
    private fun targetDifficulty(
        solvedProblems: List<Problem>,
        currentStageLabel: String,
    ): Difficulty {
        val stageSolved = solvedProblems.count { problem ->
            PythonProblemCurriculum.stageFor(problem).label == currentStageLabel
        }
        val easySolved = solvedProblems.count { problem -> problem.difficulty == Difficulty.EASY }
        val mediumSolved = solvedProblems.count { problem -> problem.difficulty == Difficulty.MEDIUM }
        return when {
            stageSolved < STAGE_EASY_GATE -> Difficulty.EASY
            mediumSolved >= MEDIUM_TO_HARD_THRESHOLD -> Difficulty.HARD
            easySolved >= EASY_TO_MEDIUM_THRESHOLD -> Difficulty.MEDIUM
            else -> Difficulty.EASY
        }
    }

    /** Finds the next later curriculum stage with unsolved problems for explanation text. */
    private fun nextUnmasteredStageLabel(
        problems: List<Problem>,
        solvedProblemIds: Set<String>,
        currentStageOrder: Int,
    ): String = PythonProblemCurriculum.stages
        .firstOrNull { stage ->
            stage.order > currentStageOrder &&
                problems.any { problem ->
                    problem.id !in solvedProblemIds &&
                        PythonProblemCurriculum.stageFor(problem).key == stage.key
                }
        }
        ?.label
        ?: "advanced practice"

    /**
     * Sorts final cards into a stable, explainable order.
     *
     * Problems appear before lessons, then curriculum stage and difficulty keep the list from jumping
     * directly into advanced MBPP tasks.
     */
    private fun recommendationComparator(): Comparator<Recommendation> =
        compareBy<Recommendation> { recommendation ->
            if (recommendation.type == RecommendationType.PROBLEM) 0 else 1
        }.thenBy { recommendation ->
            recommendation.problem?.let { problem ->
                PythonProblemCurriculum.stageFor(problem).order
            } ?: Int.MAX_VALUE
        }.thenBy { recommendation ->
            recommendation.problem?.difficulty?.rank ?: Int.MAX_VALUE
        }.thenByDescending { recommendation ->
            recommendation.score
        }.thenBy { recommendation ->
            recommendation.problem?.order?.takeIf { it > 0 } ?: Int.MAX_VALUE
        }.thenBy { recommendation ->
            recommendation.title
        }

    /** Checks whether a lesson text looks relevant to a target curriculum tag. */
    private fun Lesson.matchesTag(tag: String): Boolean {
        val haystack = "$categoryId $title $description $content".lowercase()
        val aliases = tagAliases[tag].orEmpty() + tag + PythonProblemCurriculum.labelForTag(tag)
        return aliases.any { alias -> haystack.contains(alias.lowercase()) }
    }

    /** Deduplication key so the same problem/lesson cannot appear from multiple candidate sources. */
    private val Recommendation.targetKey: String
        get() = when (type) {
            RecommendationType.PROBLEM -> "problem:${problem?.id.orEmpty()}"
            RecommendationType.LESSON -> "lesson:${lesson?.id.orEmpty()}"
        }

    /** Normalized problem tags used when no preferred curriculum tag is supplied. */
    private val Problem.normalizedTags: List<String>
        get() = tags.map { tag -> tag.normalizeTag() }

    /** Keeps lessons deterministic when Firestore returns documents without an explicit order. */
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

    private val Difficulty.displayName: String
        get() = name.lowercase().replaceFirstChar(Char::uppercase)

    /** Shared tag cleanup before matching against aliases. */
    private fun String.normalizeTag(): String =
        trim().lowercase()

    /** Default stats object used when there are no problems to recommend. */
    private fun emptyStats() = RecommendationStats(
        solvedProblems = 0,
        totalProblems = 0,
        solvedEasy = 0,
        solvedMedium = 0,
        solvedHard = 0,
        currentStage = "Syntax basics",
        nextStage = "For and while loops",
        nextDifficulty = Difficulty.EASY,
        weakestArea = "Syntax basics",
        stageSolved = 0,
        stageTotal = 0,
    )

    private companion object {
        const val DEFAULT_MAX_RESULTS = 5
        const val LEARNING_PATH_CANDIDATES = 6
        const val STAGE_EASY_GATE = 3
        const val EASY_TO_MEDIUM_THRESHOLD = 8
        const val MEDIUM_TO_HARD_THRESHOLD = 8

        val tagAliases = mapOf(
            "syntax" to listOf("syntax", "function", "return", "setup"),
            "operators" to listOf("operator", "arithmetic", "number", "integer", "math"),
            "conditionals" to listOf("condition", "if", "else", "boolean", "check"),
            "loops" to listOf("loop", "for", "while", "range"),
            "strings" to listOf("string", "text", "word", "character"),
            "lists" to listOf("list", "array", "sequence"),
            "collections" to listOf("tuple", "set", "dictionary", "dict"),
            "functions" to listOf("function", "parameter", "return"),
            "algorithms" to listOf("sort", "recursion", "matrix", "tree", "heap", "regex"),
        )
    }
}
