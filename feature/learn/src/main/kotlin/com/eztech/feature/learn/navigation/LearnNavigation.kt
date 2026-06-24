package com.eztech.feature.learn.navigation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.eztech.core.domain.model.LessonContentType
import com.eztech.feature.learn.presentation.category.LessonCategoryScreen
import com.eztech.feature.learn.presentation.list.LessonListScreen
import com.eztech.feature.learn.presentation.tutorial.TutorialArticleScreen
import com.eztech.feature.learn.presentation.video.VideoPlayerScreen

object LearnRoutes {
    const val Root = "learn"
    const val LanguageIdArg = "languageId"
    const val CategoryIdArg = "categoryId"
    const val CategoryNameArg = "categoryName"
    const val LessonIdArg = "lessonId"

    const val LessonList =
        "$Root/{$LanguageIdArg}/category/{$CategoryIdArg}?$CategoryNameArg={$CategoryNameArg}"
    const val VideoPlayer = "$Root/lesson/{$LessonIdArg}"
    const val TutorialArticle = "$Root/tutorial/{$LessonIdArg}"

    fun lessonList(
        languageId: String,
        categoryId: String,
        categoryName: String,
    ): String = "$Root/${Uri.encode(languageId)}/category/${Uri.encode(categoryId)}" +
        "?$CategoryNameArg=${Uri.encode(categoryName)}"

    fun videoPlayer(lessonId: String): String =
        "$Root/lesson/${Uri.encode(lessonId)}"

    fun tutorialArticle(lessonId: String): String =
        "$Root/tutorial/${Uri.encode(lessonId)}"
}

fun NavGraphBuilder.learnGraph(navController: NavHostController) {
    composable(LearnRoutes.Root) {
        LessonCategoryScreen(
            onCategoryClick = { category ->
                navController.navigate(
                    LearnRoutes.lessonList(
                        languageId = category.languageId,
                        categoryId = category.id,
                        categoryName = category.name,
                    ),
                )
            },
            onVideoClick = { lesson ->
                navController.navigate(LearnRoutes.videoPlayer(lesson.id))
            },
        )
    }
    composable(
        route = LearnRoutes.LessonList,
        arguments = listOf(
            navArgument(LearnRoutes.LanguageIdArg) { type = NavType.StringType },
            navArgument(LearnRoutes.CategoryIdArg) { type = NavType.StringType },
            navArgument(LearnRoutes.CategoryNameArg) {
                type = NavType.StringType
                defaultValue = "Lessons"
            },
        ),
    ) {
        LessonListScreen(
            onBackClick = navController::popBackStack,
            onLessonClick = { lesson ->
                if (lesson.type == LessonContentType.TUTORIAL) {
                    navController.navigate(LearnRoutes.tutorialArticle(lesson.id))
                } else {
                    navController.navigate(LearnRoutes.videoPlayer(lesson.id))
                }
            },
        )
    }
    composable(
        route = LearnRoutes.VideoPlayer,
        arguments = listOf(
            navArgument(LearnRoutes.LessonIdArg) { type = NavType.StringType },
        ),
    ) {
        VideoPlayerScreen(onBackClick = navController::popBackStack)
    }
    composable(
        route = LearnRoutes.TutorialArticle,
        arguments = listOf(
            navArgument(LearnRoutes.LessonIdArg) { type = NavType.StringType },
        ),
    ) {
        TutorialArticleScreen(onBackClick = navController::popBackStack)
    }
}
