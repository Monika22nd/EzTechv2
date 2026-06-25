package com.eztech.feature.problems.presentation

import androidx.lifecycle.SavedStateHandle
import com.eztech.core.common.Resource
import com.eztech.core.domain.model.CodeExecutionResult
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.DailyLoginResult
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.ProblemDraft
import com.eztech.core.domain.model.ProblemSubmission
import com.eztech.core.domain.model.GamificationProgress
import com.eztech.core.domain.model.LeaderboardEntry
import com.eztech.core.domain.model.ProblemCompletion
import com.eztech.core.domain.model.SubmissionResult
import com.eztech.core.domain.model.SubmissionStatus
import com.eztech.core.domain.model.TestCase
import com.eztech.core.domain.model.User
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.repository.CodeExecutionRepository
import com.eztech.core.domain.repository.ProblemRepository
import com.eztech.core.domain.repository.ProblemWorkspaceRepository
import com.eztech.core.domain.repository.GamificationRepository
import com.eztech.core.domain.usecase.gamification.CompleteProblemUseCase
import com.eztech.core.domain.usecase.gamification.UnlockEligibleBadgesUseCase
import com.eztech.core.domain.usecase.problem.GetCodeDraftUseCase
import com.eztech.core.domain.usecase.problem.GetProblemDetailUseCase
import com.eztech.core.domain.usecase.problem.GetProblemSubmissionHistoryUseCase
import com.eztech.core.domain.usecase.problem.GetProblemsUseCase
import com.eztech.core.domain.usecase.problem.GetVisibleTestCasesUseCase
import com.eztech.core.domain.usecase.problem.RecordProblemSubmissionUseCase
import com.eztech.core.domain.usecase.problem.RunCustomInputUseCase
import com.eztech.core.domain.usecase.problem.SaveCodeDraftUseCase
import com.eztech.core.domain.usecase.problem.SubmitSolutionUseCase
import com.eztech.feature.problems.navigation.ProblemsRoutes
import com.eztech.feature.problems.presentation.list.ProblemListViewModel
import com.eztech.feature.problems.presentation.solve.ProblemSolveViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProblemsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `list loads and applies difficulty filter`() = runTest(dispatcher) {
        val repository = FakeProblemRepository()
        val viewModel = ProblemListViewModel(GetProblemsUseCase(repository))
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.problems.size)

        viewModel.selectDifficulty(Difficulty.HARD)
        advanceUntilIdle()

        assertEquals(Difficulty.HARD, viewModel.uiState.value.selectedDifficulty)
        assertEquals(listOf("hard"), viewModel.uiState.value.problems.map(Problem::id))
    }

    @Test
    fun `solve loads starter code and publishes accepted result`() = runTest(dispatcher) {
        val problemRepository = FakeProblemRepository()
        val codeExecutionRepository = FakeCodeExecutionRepository()
        val workspaceRepository = FakeProblemWorkspaceRepository()
        val viewModel = ProblemSolveViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(ProblemsRoutes.ProblemIdArg to "easy"),
            ),
            getProblem = GetProblemDetailUseCase(problemRepository),
            getVisibleTestCases = GetVisibleTestCasesUseCase(problemRepository),
            submitSolution = SubmitSolutionUseCase(
                problemRepository = problemRepository,
                codeExecutionRepository = codeExecutionRepository,
            ),
            runCustomInputUseCase = RunCustomInputUseCase(codeExecutionRepository),
            authRepository = FakeAuthRepository(),
            completeProblem = FakeGamificationRepository().let { repository ->
                CompleteProblemUseCase(
                    gamificationRepository = repository,
                    unlockEligibleBadges = UnlockEligibleBadgesUseCase(repository),
                )
            },
            getCodeDraft = GetCodeDraftUseCase(workspaceRepository),
            saveCodeDraft = SaveCodeDraftUseCase(workspaceRepository),
            recordProblemSubmission = RecordProblemSubmissionUseCase(workspaceRepository),
            getSubmissionHistory = GetProblemSubmissionHistoryUseCase(workspaceRepository),
        )
        advanceUntilIdle()

        assertEquals("print(input())", viewModel.uiState.value.code)

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(
            SubmissionStatus.ACCEPTED,
            viewModel.uiState.value.submissionResult?.status,
        )
        assertEquals(20, viewModel.uiState.value.completion?.awardedExp)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals(1, workspaceRepository.submissionsSaved)
    }

    private class FakeProblemRepository : ProblemRepository {
        private val problems = listOf(
            problem("easy", Difficulty.EASY),
            problem("hard", Difficulty.HARD),
        )
        private val tests = listOf(
            TestCase(
                id = "visible",
                input = "hello",
                expectedOutput = "hello",
                isHidden = false,
            ),
            TestCase(
                id = "hidden",
                input = "secret",
                expectedOutput = "secret",
                isHidden = true,
            ),
        )

        override fun observeProblems(difficulty: Difficulty?): Flow<Resource<List<Problem>>> =
            flowOf(
                Resource.Success(
                    problems.filter { problem ->
                        difficulty == null || problem.difficulty == difficulty
                    },
                ),
            )

        override suspend fun getProblemById(problemId: String): Resource<Problem> =
            Resource.Success(problems.first { problem -> problem.id == problemId })

        override suspend fun getTestCases(problemId: String): Resource<List<TestCase>> =
            Resource.Success(tests)
    }

    private class FakeCodeExecutionRepository : CodeExecutionRepository {
        override suspend fun executeCode(
            code: String,
            stdin: String,
        ): Resource<CodeExecutionResult> = Resource.Success(
            CodeExecutionResult(
                stdout = stdin,
                stderr = "",
                exitCode = 0,
                executionTimeMs = 4,
            ),
        )
    }

    private class FakeProblemWorkspaceRepository : ProblemWorkspaceRepository {
        var submissionsSaved = 0

        override suspend fun getCodeDraft(
            userId: String,
            problemId: String,
        ): Resource<ProblemDraft?> = Resource.Success(null)

        override suspend fun saveCodeDraft(
            userId: String,
            problemId: String,
            code: String,
        ): Resource<Unit> = Resource.Success(Unit)

        override suspend fun recordSubmission(
            userId: String,
            problemId: String,
            result: SubmissionResult,
        ): Resource<Unit> {
            submissionsSaved += 1
            return Resource.Success(Unit)
        }

        override fun observeSubmissionHistory(
            userId: String,
            problemId: String,
            limit: Int,
        ): Flow<Resource<List<ProblemSubmission>>> = flowOf(Resource.Success(emptyList()))
    }

    private class FakeAuthRepository : AuthRepository {
        override fun observeCurrentUser(): Flow<User?> = flowOf(
            User(uid = "user", name = "User", email = "user@example.com"),
        )

        override suspend fun login(email: String, password: String) =
            Resource.Error("Not used")

        override suspend fun register(name: String, email: String, password: String) =
            Resource.Error("Not used")

        override suspend fun sendPasswordReset(email: String) = Resource.Success(Unit)

        override suspend fun logout() = Unit
    }

    private class FakeGamificationRepository : GamificationRepository {
        private val progress = GamificationProgress(
            totalExp = 20,
            level = 1,
            solvedCount = 1,
            hardSolvedCount = 0,
            currentStreak = 0,
            watchedLessonCount = 0,
        )

        override suspend fun awardExp(userId: String, amount: Int, reason: String) =
            Resource.Success(Unit)

        override suspend fun getBadges(userId: String): Resource<List<Badge>> =
            Resource.Success(emptyList())

        override suspend fun unlockBadge(userId: String, badgeId: String) =
            Resource.Success(Unit)

        override suspend fun recordProblemSolved(
            userId: String,
            problemId: String,
            difficulty: Difficulty,
            expReward: Int,
            solveDurationSeconds: Int,
        ) = Resource.Success(
            ProblemCompletion(
                firstSolve = true,
                awardedExp = expReward,
                progress = progress,
            ),
        )

        override suspend fun recordDailyLogin(
            userId: String,
            today: String,
            yesterday: String,
            expReward: Int,
        ) = Resource.Success(
            DailyLoginResult(
                firstLoginToday = true,
                awardedExp = expReward,
                progress = progress,
            ),
        )

        override fun observeLeaderboard(): Flow<Resource<List<LeaderboardEntry>>> =
            flowOf(Resource.Success(emptyList()))

        override fun observeUserLeaderboardEntry(
            userId: String,
        ): Flow<Resource<LeaderboardEntry?>> = flowOf(Resource.Success(null))
    }

    private companion object {
        fun problem(id: String, difficulty: Difficulty) = Problem(
            id = id,
            title = id,
            description = "Description",
            difficulty = difficulty,
            constraints = emptyList(),
            starterCode = "print(input())",
            solutionCode = "print(input())",
        )
    }
}
