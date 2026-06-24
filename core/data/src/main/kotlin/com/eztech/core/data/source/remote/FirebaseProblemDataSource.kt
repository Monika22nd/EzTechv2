package com.eztech.core.data.source.remote

import android.util.Log
import com.eztech.core.data.source.local.ProblemDataSource
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.TestCase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
internal class FirebaseProblemDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ProblemDataSource {
    override suspend fun getProblems(difficulty: Difficulty?): List<Problem> =
        firestore.collection(PROBLEMS)
            .orderBy(ORDER)
            .limit(MAX_PROBLEM_COUNT)
            .get()
            .await()
            .documents
            .map { document -> document.toProblem() }
            .filter { problem -> difficulty == null || problem.difficulty == difficulty }
            .sortedBy(Problem::order)
            .also { problems ->
                require(problems.isNotEmpty()) { "Firestore does not contain matching problems." }
                Log.i(TAG, "Loaded ${problems.size} problems from Firestore.")
            }

    override suspend fun getProblem(problemId: String): Problem {
        val snapshot = firestore.collection(PROBLEMS).document(problemId).get().await()
        require(snapshot.exists()) { "Problem '$problemId' does not exist in Firestore." }
        return snapshot.toProblem()
    }

    override suspend fun getTestCases(problemId: String): List<TestCase> =
        firestore.collection(PROBLEMS)
            .document(problemId)
            .collection(TEST_CASES)
            .get()
            .await()
            .documents
            .map { document -> document.toTestCase() }
            .sortedBy(TestCase::id)
            .also { testCases ->
                require(testCases.isNotEmpty()) {
                    "Problem '$problemId' does not contain test cases in Firestore."
                }
            }

    private fun DocumentSnapshot.toProblem() = Problem(
        id = id,
        title = requiredString(TITLE),
        description = requiredString(DESCRIPTION),
        difficulty = runCatching {
            Difficulty.valueOf(requiredString(DIFFICULTY).uppercase())
        }.getOrElse {
            error("Problem '$id' has an invalid difficulty.")
        },
        constraints = stringList(CONSTRAINTS),
        starterCode = requiredString(STARTER_CODE),
        solutionCode = getString(SOLUTION_CODE).orEmpty(),
        tags = stringList(TAGS),
        order = getLong(ORDER)?.toInt() ?: 0,
    )

    private fun DocumentSnapshot.toTestCase() = TestCase(
        id = id,
        input = getString(INPUT).orEmpty(),
        expectedOutput = getString(EXPECTED_OUTPUT).orEmpty(),
        isHidden = getBoolean(IS_HIDDEN) ?: false,
    )

    private fun DocumentSnapshot.requiredString(field: String): String =
        requireNotNull(getString(field)?.takeIf(String::isNotBlank)) {
            "Firestore document '$id' is missing '$field'."
        }

    private fun DocumentSnapshot.stringList(field: String): List<String> =
        (get(field) as? List<*>)?.mapNotNull { item -> item as? String }.orEmpty()

    private companion object {
        const val PROBLEMS = "problems"
        const val TEST_CASES = "test_cases"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val DIFFICULTY = "difficulty"
        const val CONSTRAINTS = "constraints"
        const val STARTER_CODE = "starterCode"
        const val SOLUTION_CODE = "solutionCode"
        const val TAGS = "tags"
        const val ORDER = "order"
        const val INPUT = "input"
        const val EXPECTED_OUTPUT = "expectedOutput"
        const val IS_HIDDEN = "isHidden"
        const val MAX_PROBLEM_COUNT = 200L
        const val TAG = "EzTechFirestore"
    }
}
