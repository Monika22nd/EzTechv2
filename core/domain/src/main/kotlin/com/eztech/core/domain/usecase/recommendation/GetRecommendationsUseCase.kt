package com.eztech.core.domain.usecase.recommendation

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonContentType
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.Recommendation
import com.eztech.core.domain.model.User
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.repository.LessonRepository
import com.eztech.core.domain.repository.ProblemRepository
import com.eztech.core.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetRecommendationsUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val lessonRepository: LessonRepository,
    private val problemRepository: ProblemRepository,
    private val engine: RecommendationEngine,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        languageId: String = PYTHON_LANGUAGE_ID,
        maxResults: Int = DEFAULT_MAX_RESULTS,
    ): Flow<Resource<List<Recommendation>>> =
        authRepository.observeCurrentUser().flatMapLatest { authUser ->
            if (authUser == null) {
                flowOf(Resource.Error("Please sign in to view recommendations."))
            } else {
                combine(
                    userRepository.observeUserProfile(authUser.uid),
                    lessonRepository.observeLessonsByType(languageId, LessonContentType.VIDEO),
                    lessonRepository.observeLessonsByType(languageId, LessonContentType.TUTORIAL),
                    problemRepository.observeProblems(),
                ) { userResource, videosResource, tutorialsResource, problemsResource ->
                    buildRecommendations(
                        userResource = userResource,
                        videosResource = videosResource,
                        tutorialsResource = tutorialsResource,
                        problemsResource = problemsResource,
                        maxResults = maxResults,
                    )
                }
            }
        }

    private fun buildRecommendations(
        userResource: Resource<User>,
        videosResource: Resource<List<Lesson>>,
        tutorialsResource: Resource<List<Lesson>>,
        problemsResource: Resource<List<Problem>>,
        maxResults: Int,
    ): Resource<List<Recommendation>> {
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
        val lessons = (videos + tutorials).distinctBy(Lesson::id)

        return Resource.Success(
            engine.generateRecommendations(
                user = user,
                problems = problems,
                lessons = lessons,
                maxResults = maxResults,
            ),
        )
    }

    private companion object {
        const val PYTHON_LANGUAGE_ID = "python"
        const val DEFAULT_MAX_RESULTS = 5
    }
}
