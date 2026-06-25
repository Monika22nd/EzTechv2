package com.eztech.core.domain.usecase.recommendation

import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonContentType
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.RecommendationType
import com.eztech.core.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationEngineTest {
    private val engine = RecommendationEngine()

    @Test
    fun `new user receives first unsolved learning path problems`() {
        val recommendations = engine.generateRecommendations(
            user = user(),
            problems = listOf(
                problem(id = "p2", order = 2),
                problem(id = "p1", order = 1),
            ),
            lessons = emptyList(),
            maxResults = 2,
        )

        assertEquals(listOf("p1", "p2"), recommendations.map { item -> item.problem?.id })
        assertTrue(recommendations.first().reason.contains("just starting out"))
    }

    @Test
    fun `user who solved enough easy problems receives medium recommendation`() {
        val solvedEasy = (1..5).map { index ->
            problem(id = "easy_$index", difficulty = Difficulty.EASY, order = index)
        }
        val medium = problem(
            id = "medium",
            difficulty = Difficulty.MEDIUM,
            tags = listOf("lists"),
            order = 6,
        )

        val recommendations = engine.generateRecommendations(
            user = user(solvedProblemIds = solvedEasy.map(Problem::id)),
            problems = solvedEasy + medium,
            lessons = emptyList(),
            maxResults = 5,
        )

        assertTrue(
            recommendations.any { item ->
                item.problem?.id == "medium" &&
                    item.reason.contains("You solved 5 Easy")
            },
        )
    }

    @Test
    fun `engine recommends problem types the user has not practiced`() {
        val solvedListProblem = problem(
            id = "list_1",
            tags = listOf("lists"),
            order = 1,
        )
        val stringProblem = problem(
            id = "string_1",
            tags = listOf("strings"),
            order = 2,
        )

        val recommendations = engine.generateRecommendations(
            user = user(solvedProblemIds = listOf(solvedListProblem.id)),
            problems = listOf(solvedListProblem, stringProblem),
            lessons = emptyList(),
            maxResults = 3,
        )

        assertTrue(
            recommendations.any { item ->
                item.problem?.id == stringProblem.id &&
                    item.reason.contains("0 Strings")
            },
        )
    }

    @Test
    fun `engine recommends related unwatched lesson for focused tag`() {
        val stringProblem = problem(
            id = "string_1",
            tags = listOf("strings"),
            order = 1,
        )
        val lesson = Lesson(
            id = "lesson_strings",
            languageId = "python",
            categoryId = "python_strings",
            title = "Working with strings",
            order = 1,
            durationSeconds = 300,
            type = LessonContentType.TUTORIAL,
        )

        val recommendations = engine.generateRecommendations(
            user = user(),
            problems = listOf(stringProblem),
            lessons = listOf(lesson),
            maxResults = 5,
        )

        assertTrue(
            recommendations.any { item ->
                item.type == RecommendationType.LESSON &&
                    item.lesson?.id == lesson.id
            },
        )
    }

    private fun user(
        solvedProblemIds: List<String> = emptyList(),
        watchedLessonIds: List<String> = emptyList(),
    ) = User(
        uid = "user",
        name = "User",
        email = "user@example.com",
        solvedProblemIds = solvedProblemIds,
        watchedLessonIds = watchedLessonIds,
    )

    private fun problem(
        id: String,
        difficulty: Difficulty = Difficulty.EASY,
        tags: List<String> = listOf("lists"),
        order: Int = 1,
    ) = Problem(
        id = id,
        title = id,
        description = "Description",
        difficulty = difficulty,
        constraints = emptyList(),
        starterCode = "def solve():\n    pass",
        solutionCode = "def solve():\n    return True",
        tags = tags,
        order = order,
    )
}
