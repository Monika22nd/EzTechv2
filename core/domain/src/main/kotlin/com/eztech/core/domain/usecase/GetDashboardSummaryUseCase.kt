package com.eztech.core.domain.usecase

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.DashboardSummary
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonContentType
import com.eztech.core.domain.model.LeaderboardEntry
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.PythonProblemCurriculum
import com.eztech.core.domain.model.User
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.repository.GamificationRepository
import com.eztech.core.domain.repository.LessonRepository
import com.eztech.core.domain.repository.ProblemRepository
import com.eztech.core.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Builds the Home dashboard summary from independent domain streams.
 *
 * The dashboard intentionally has one use case instead of letting UI combine repositories, because
 * rank, next lesson, next problem, and progress counts must be calculated consistently.
 */
class GetDashboardSummaryUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val lessonRepository: LessonRepository,
    private val problemRepository: ProblemRepository,
    private val gamificationRepository: GamificationRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    /** Observes the signed-in user's dashboard and keeps it live as Firestore data changes. */
    operator fun invoke(languageId: String = PYTHON_LANGUAGE_ID): Flow<Resource<DashboardSummary>> =
        authRepository.observeCurrentUser().flatMapLatest { authUser ->
            if (authUser == null) {
                flowOf(Resource.Error("Please sign in to view your dashboard."))
            } else {
                combine(
                    userRepository.observeUserProfile(authUser.uid),
                    lessonRepository.observeLessonsByType(languageId, LessonContentType.VIDEO),
                    lessonRepository.observeLessonsByType(languageId, LessonContentType.TUTORIAL),
                    problemRepository.observeProblems(),
                    gamificationRepository.observeLeaderboard(),
                ) { userResource, videosResource, tutorialsResource, problemsResource, leaderboardResource ->
                    buildSummary(
                        userResource = userResource,
                        videosResource = videosResource,
                        tutorialsResource = tutorialsResource,
                        problemsResource = problemsResource,
                        leaderboardResource = leaderboardResource,
                    )
                }
            }
        }

    /**
     * Merges resource states and computes a display-ready DashboardSummary.
     *
     * Blocking errors from user/lesson/problem data are returned immediately. Leaderboard errors are
     * treated as non-blocking by falling back to the rank stored on the user profile.
     */
    private fun buildSummary(
        userResource: Resource<User>,
        videosResource: Resource<List<Lesson>>,
        tutorialsResource: Resource<List<Lesson>>,
        problemsResource: Resource<List<Problem>>,
        leaderboardResource: Resource<List<LeaderboardEntry>>,
    ): Resource<DashboardSummary> {
        val blockingError = listOf(
            userResource,
            videosResource,
            tutorialsResource,
            problemsResource,
        ).filterIsInstance<Resource.Error>().firstOrNull()
        if (blockingError != null) return blockingError

        val stillLoading = listOf(
            userResource,
            videosResource,
            tutorialsResource,
            problemsResource,
        ).any { resource -> resource is Resource.Loading }
        if (stillLoading) return Resource.Loading

        val user = (userResource as? Resource.Success<User>)?.data ?: return Resource.Loading
        val videos = (videosResource as? Resource.Success<List<Lesson>>)?.data.orEmpty()
        val tutorials = (tutorialsResource as? Resource.Success<List<Lesson>>)?.data.orEmpty()
        val problems = (problemsResource as? Resource.Success<List<Problem>>)?.data.orEmpty()
        val leaderboard = (leaderboardResource as? Resource.Success<List<LeaderboardEntry>>)
            ?.data
            .orEmpty()

        val lessons = (videos + tutorials).distinctBy(Lesson::id).sortedLessonsByOrder()
        val watchedLessonIds = user.watchedLessonIds.toSet()
        val solvedProblemIds = user.solvedProblemIds.toSet()
        val solvedProblemCount = problems.count { problem -> problem.id in solvedProblemIds }
            .coerceAtLeast(user.solvedCount)
            .coerceAtMost(problems.size.takeIf { it > 0 } ?: Int.MAX_VALUE)
        val rank = leaderboard.firstOrNull { entry -> entry.userId == user.uid }?.rank ?: user.rank

        return Resource.Success(
            DashboardSummary(
                userName = user.name.ifBlank { "PyQuest Learner" },
                level = user.level,
                exp = user.exp,
                expToNextLevel = user.expToNextLevel,
                expProgressFraction = user.expProgressFraction.coerceIn(0f, 1f),
                currentStreak = user.currentStreak,
                rank = rank,
                completedLessonCount = lessons.count { lesson ->
                    lesson.watched || lesson.id in watchedLessonIds
                }.coerceAtMost(lessons.size),
                totalLessonCount = lessons.size,
                videoLessonCount = videos.size,
                tutorialLessonCount = tutorials.size,
                solvedProblemCount = solvedProblemCount,
                totalProblemCount = problems.size,
                nextLesson = lessons.firstOrNull { lesson ->
                    !lesson.watched && lesson.id !in watchedLessonIds
                },
                nextProblem = PythonProblemCurriculum.sorted(problems).firstOrNull { problem ->
                    problem.id !in solvedProblemIds
                },
            ),
        )
    }

    /** Keeps lesson continuation deterministic even if Firestore returns documents unordered. */
    private fun List<Lesson>.sortedLessonsByOrder(): List<Lesson> =
        sortedWith(compareBy<Lesson> { lesson ->
            lesson.order.takeIf { it > 0 } ?: Int.MAX_VALUE
        }.thenBy { lesson -> lesson.title })

    private companion object {
        const val PYTHON_LANGUAGE_ID = "python"
    }
}
