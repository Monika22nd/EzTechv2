package com.eztech.core.data.source.local

import android.content.Context
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonCategory
import com.eztech.core.domain.model.ProgrammingLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Singleton
internal class LocalLessonDataSource @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val applicationContext = context.applicationContext
    private val preferences = applicationContext.getSharedPreferences(
        PROGRESS_PREFERENCES,
        Context.MODE_PRIVATE,
    )
    private val _progressVersion = MutableStateFlow(0L)
    val progressVersion: StateFlow<Long> = _progressVersion.asStateFlow()

    private val seedData: LessonSeedData by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        loadSeedData()
    }

    suspend fun getLanguages(): List<ProgrammingLanguage> = withContext(Dispatchers.IO) {
        seedData.languages
            .filter(ProgrammingLanguage::isEnabled)
            .sortedBy(ProgrammingLanguage::order)
    }

    suspend fun getCategories(languageId: String): List<LessonCategory> =
        withContext(Dispatchers.IO) {
            seedData.categories
                .asSequence()
                .filter { category -> category.languageId == languageId }
                .map { category ->
                    category.copy(
                        lessonCount = seedData.lessons.count { lesson ->
                            lesson.languageId == languageId && lesson.categoryId == category.id
                        },
                    )
                }
                .sortedBy(LessonCategory::order)
                .toList()
        }

    suspend fun getLessons(
        languageId: String,
        categoryId: String,
        userId: String?,
    ): List<Lesson> = withContext(Dispatchers.IO) {
        val watchedLessonIds = userId
            ?.let(::getWatchedLessonIds)
            .orEmpty()

        seedData.lessons
            .asSequence()
            .filter { lesson ->
                lesson.languageId == languageId && lesson.categoryId == categoryId
            }
            .map { lesson -> lesson.copy(watched = lesson.id in watchedLessonIds) }
            .sortedBy(Lesson::order)
            .toList()
    }

    suspend fun getLesson(
        lessonId: String,
        userId: String?,
    ): Lesson = withContext(Dispatchers.IO) {
        val lesson = seedData.lessons.firstOrNull { item -> item.id == lessonId }
            ?: error("Lesson '$lessonId' does not exist.")
        val watched = userId
            ?.let(::getWatchedLessonIds)
            ?.contains(lessonId)
            ?: false

        lesson.copy(watched = watched)
    }

    suspend fun markAsWatched(userId: String, lessonId: String) = withContext(Dispatchers.IO) {
        require(userId.isNotBlank()) { "A user ID is required to save lesson progress." }
        require(seedData.lessons.any { lesson -> lesson.id == lessonId }) {
            "Lesson '$lessonId' does not exist."
        }

        val watchedLessonIds = getWatchedLessonIds(userId).toMutableSet()
        if (watchedLessonIds.add(lessonId)) {
            preferences.edit()
                .putStringSet(progressKey(userId), watchedLessonIds)
                .apply()
            _progressVersion.update { version -> version + 1 }
        }
    }

    private fun getWatchedLessonIds(userId: String): Set<String> =
        preferences.getStringSet(progressKey(userId), emptySet())
            ?.toSet()
            .orEmpty()

    private fun loadSeedData(): LessonSeedData {
        val root = applicationContext.assets.open(SEED_DATA_PATH)
            .bufferedReader()
            .use { reader -> JSONObject(reader.readText()) }

        val data = LessonSeedData(
            languages = root.getJSONArray("languages").mapObjects(::parseLanguage),
            categories = root.getJSONArray("categories").mapObjects(::parseCategory),
            lessons = root.getJSONArray("lessons").mapObjects(::parseLesson),
        )
        validate(data)
        return data
    }

    private fun parseLanguage(json: JSONObject) = ProgrammingLanguage(
        id = json.getString("id"),
        name = json.getString("name"),
        description = json.optString("description"),
        iconUrl = json.optionalString("iconUrl"),
        order = json.optInt("order"),
        isEnabled = json.optBoolean("isEnabled", true),
    )

    private fun parseCategory(json: JSONObject) = LessonCategory(
        id = json.getString("id"),
        languageId = json.getString("languageId"),
        name = json.getString("name"),
        lessonCount = 0,
        description = json.optString("description"),
        iconUrl = json.optionalString("iconUrl"),
        order = json.optInt("order"),
    )

    private fun parseLesson(json: JSONObject): Lesson {
        val videoId = json.getString("videoId")
        return Lesson(
            id = json.getString("id"),
            languageId = json.getString("languageId"),
            categoryId = json.getString("categoryId"),
            title = json.getString("title"),
            videoId = videoId,
            order = json.optInt("order"),
            durationSeconds = json.optInt("durationSeconds"),
            description = json.optString("description"),
            sourceName = json.optString("sourceName"),
            thumbnailUrl = json.optionalString("thumbnailUrl")
                ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
        )
    }

    private fun validate(data: LessonSeedData) {
        val languageIds = data.languages.map(ProgrammingLanguage::id).toSet()
        val categoryIds = data.categories.map(LessonCategory::id).toSet()

        require(languageIds.size == data.languages.size) { "Language IDs must be unique." }
        require(categoryIds.size == data.categories.size) { "Category IDs must be unique." }
        require(data.lessons.map(Lesson::id).toSet().size == data.lessons.size) {
            "Lesson IDs must be unique."
        }
        require(data.categories.all { category -> category.languageId in languageIds }) {
            "Every category must reference an existing language."
        }
        require(data.lessons.all { lesson ->
            lesson.languageId in languageIds && lesson.categoryId in categoryIds
        }) {
            "Every lesson must reference an existing language and category."
        }
    }

    private fun progressKey(userId: String) = "watched_lessons_$userId"

    private fun JSONObject.optionalString(key: String): String? =
        optString(key).takeIf(String::isNotBlank)

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
        List(length()) { index -> transform(getJSONObject(index)) }

    private data class LessonSeedData(
        val languages: List<ProgrammingLanguage>,
        val categories: List<LessonCategory>,
        val lessons: List<Lesson>,
    )

    private companion object {
        const val SEED_DATA_PATH = "seed_data/lessons.json"
        const val PROGRESS_PREFERENCES = "lesson_progress"
    }
}
