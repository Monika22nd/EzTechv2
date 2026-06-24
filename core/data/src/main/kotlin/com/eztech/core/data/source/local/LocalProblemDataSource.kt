package com.eztech.core.data.source.local

import android.content.Context
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.TestCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Singleton
internal class LocalProblemDataSource @Inject constructor(
    @ApplicationContext context: Context,
) : ProblemDataSource {
    private val applicationContext = context.applicationContext

    private val seedData: ProblemSeedData by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        loadSeedData()
    }

    override suspend fun getProblems(difficulty: Difficulty?): List<Problem> =
        withContext(Dispatchers.IO) {
            seedData.problems
                .asSequence()
                .filter { problem -> difficulty == null || problem.difficulty == difficulty }
                .sortedBy(Problem::order)
                .toList()
        }

    override suspend fun getProblem(problemId: String): Problem = withContext(Dispatchers.IO) {
        seedData.problems.firstOrNull { problem -> problem.id == problemId }
            ?: error("Problem '$problemId' does not exist.")
    }

    override suspend fun getTestCases(problemId: String): List<TestCase> =
        withContext(Dispatchers.IO) {
            if (seedData.problems.none { problem -> problem.id == problemId }) {
                error("Problem '$problemId' does not exist.")
            }

            seedData.testCases
                .filter { item -> item.problemId == problemId }
                .map(SeedTestCase::testCase)
        }

    private fun loadSeedData(): ProblemSeedData {
        val root = applicationContext.assets.open(SEED_DATA_PATH)
            .bufferedReader()
            .use { reader -> JSONObject(reader.readText()) }
        val problemArray = root.getJSONArray("problems")
        val problems = List(minOf(problemArray.length(), MAX_PROBLEM_COUNT)) { index ->
            parseProblem(problemArray.getJSONObject(index), order = index + 1)
        }
        val problemIds = problems.mapTo(mutableSetOf(), Problem::id)
        val data = ProblemSeedData(
            problems = problems,
            testCases = root.getJSONArray("testCases")
                .mapObjects(::parseTestCase)
                .filter { item -> item.problemId in problemIds },
        )
        validate(data)
        return data
    }

    private fun parseProblem(json: JSONObject, order: Int) = Problem(
        id = json.getString("id"),
        title = json.getString("title"),
        description = json.getString("description"),
        difficulty = runCatching {
            Difficulty.valueOf(json.getString("difficulty").uppercase())
        }.getOrElse {
            error("Invalid difficulty for problem '${json.optString("id")}'.")
        },
        constraints = json.getJSONArray("constraints").mapStrings(),
        starterCode = json.getString("starterCode"),
        solutionCode = json.getString("solutionCode"),
        tags = json.optJSONArray("tags")?.mapStrings().orEmpty(),
        order = order,
    )

    private fun parseTestCase(json: JSONObject) = SeedTestCase(
        problemId = json.getString("problemId"),
        testCase = TestCase(
            id = json.getString("id"),
            input = json.getString("input"),
            expectedOutput = json.getString("expectedOutput"),
            isHidden = json.optBoolean("isHidden", false),
        ),
    )

    private fun validate(data: ProblemSeedData) {
        val problemIds = data.problems.map(Problem::id)
        val testCaseIds = data.testCases.map { item -> item.testCase.id }

        require(problemIds.size == problemIds.toSet().size) { "Problem IDs must be unique." }
        require(testCaseIds.size == testCaseIds.toSet().size) { "Test case IDs must be unique." }
        require(data.problems.all { problem ->
            problem.id.isNotBlank() &&
                problem.title.isNotBlank() &&
                problem.description.isNotBlank() &&
                problem.starterCode.isNotBlank() &&
                problem.solutionCode.isNotBlank()
        }) { "Every problem must contain all required fields." }
        require(data.testCases.all { item -> item.problemId in problemIds }) {
            "Every test case must reference an existing problem."
        }
        require(problemIds.all { problemId ->
            data.testCases.any { item -> item.problemId == problemId && !item.testCase.isHidden }
        }) { "Every problem must contain at least one visible test case." }
    }

    private fun JSONArray.mapStrings(): List<String> =
        List(length()) { index -> getString(index) }

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
        List(length()) { index -> transform(getJSONObject(index)) }

    private data class ProblemSeedData(
        val problems: List<Problem>,
        val testCases: List<SeedTestCase>,
    )

    private data class SeedTestCase(
        val problemId: String,
        val testCase: TestCase,
    )

    private companion object {
        const val SEED_DATA_PATH = "seed_data/problems.json"
        const val MAX_PROBLEM_COUNT = 200
    }
}
