package com.eztech.core.domain.usecase

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.DashboardSummary
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonContentType
import com.eztech.core.domain.model.LeaderboardEntry
import com.eztech.core.domain.model.Problem
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

class GetDashboardSummaryUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val lessonRepository: LessonRepository,
    private val problemRepository: ProblemRepository,
    private val gamificationRepository: GamificationRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
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
                userName = user.name.ifBlank { "EzTech Learner" },
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
                nextProblem = problems.sortedProblemsByOrder().firstOrNull { problem ->
                    problem.id !in solvedProblemIds
                },
            ),
        )
    }

    private fun List<Lesson>.sortedLessonsByOrder(): List<Lesson> =
        sortedWith(compareBy<Lesson> { lesson ->
            lesson.order.takeIf { it > 0 } ?: Int.MAX_VALUE
        }.thenBy { lesson -> lesson.title })

    private fun List<Problem>.sortedProblemsByOrder(): List<Problem> =
        sortedWith(compareBy<Problem> { problem ->
            problem.order.takeIf { it > 0 } ?: Int.MAX_VALUE
        }.thenBy { problem -> problem.title })

    private companion object {
        const val PYTHON_LANGUAGE_ID = "python"
    }
}
