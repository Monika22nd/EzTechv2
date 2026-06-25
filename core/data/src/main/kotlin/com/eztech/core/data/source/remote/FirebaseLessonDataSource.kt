package com.eztech.core.data.source.remote

import android.util.Log
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonCategory
import com.eztech.core.domain.model.LessonContentType
import com.eztech.core.domain.model.ProgrammingLanguage
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
internal class FirebaseLessonDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private var languageCache: List<ProgrammingLanguage>? = null
    private val categoryCache = mutableMapOf<String, List<LessonCategory>>()
    private val lessonCache = mutableMapOf<String, List<Lesson>>()

    suspend fun getLanguages(): List<ProgrammingLanguage> {
        languageCache?.let { return it }
        return firestore.collection(PROGRAMMING_LANGUAGES)
            .orderBy(ORDER)
            .get()
            .await()
            .documents
            .map { document -> document.toProgrammingLanguage() }
            .filter(ProgrammingLanguage::isEnabled)
            .also { languages ->
                require(languages.isNotEmpty()) { "Firestore does not contain programming languages." }
                Log.i(TAG, "Loaded ${languages.size} programming languages from Firestore.")
            }
            .also { languages -> languageCache = languages }
    }

    suspend fun getCategories(languageId: String): List<LessonCategory> {
        categoryCache[languageId]?.let { return it }
        val lessons = getLessonsForLanguage(languageId)
        return firestore.collection(LESSON_CATEGORIES)
            .whereEqualTo(LANGUAGE_ID, languageId)
            .get()
            .await()
            .documents
            .map { document -> document.toLessonCategory() }
            .map { category ->
                category.copy(
                    lessonCount = lessons.count { lesson -> lesson.categoryId == category.id },
                )
            }
            .sortedBy(LessonCategory::order)
            .also { categories ->
                require(categories.isNotEmpty()) {
                    "Firestore does not contain categories for '$languageId'."
                }
                Log.i(TAG, "Loaded ${categories.size} lesson categories from Firestore.")
            }
            .also { categories -> categoryCache[languageId] = categories }
    }

    suspend fun getLessons(
        languageId: String,
        categoryId: String,
        watchedLessonIds: Set<String>,
        bookmarkedLessonIds: Set<String>,
    ): List<Lesson> = getLessonsForLanguage(languageId)
        .asSequence()
        .filter { lesson -> lesson.categoryId == categoryId }
        .map { lesson ->
            lesson.copy(
                watched = lesson.id in watchedLessonIds,
                bookmarked = lesson.id in bookmarkedLessonIds,
            )
        }
        .sortedBy(Lesson::order)
        .toList()

    suspend fun getLessonsByType(
        languageId: String,
        type: LessonContentType,
        watchedLessonIds: Set<String>,
        bookmarkedLessonIds: Set<String>,
    ): List<Lesson> = getLessonsForLanguage(languageId)
        .asSequence()
        .filter { lesson -> lesson.type == type }
        .map { lesson ->
            lesson.copy(
                watched = lesson.id in watchedLessonIds,
                bookmarked = lesson.id in bookmarkedLessonIds,
            )
        }
        .sortedWith(compareBy<Lesson> { it.categoryId }.thenBy(Lesson::order))
        .toList()

    suspend fun getLesson(
        lessonId: String,
        watchedLessonIds: Set<String>,
        bookmarkedLessonIds: Set<String>,
    ): Lesson {
        val snapshot = firestore.collection(LESSONS)
            .document(lessonId)
            .get()
            .await()
        require(snapshot.exists()) { "Lesson '$lessonId' does not exist in Firestore." }
        return snapshot.toLesson().copy(
            watched = lessonId in watchedLessonIds,
            bookmarked = lessonId in bookmarkedLessonIds,
        )
    }

    suspend fun getBookmarkedLessons(
        languageId: String,
        watchedLessonIds: Set<String>,
        bookmarkedLessonIds: Set<String>,
    ): List<Lesson> = getLessonsForLanguage(languageId)
        .asSequence()
        .filter { lesson -> lesson.id in bookmarkedLessonIds }
        .map { lesson ->
            lesson.copy(
                watched = lesson.id in watchedLessonIds,
                bookmarked = true,
            )
        }
        .sortedWith(compareBy<Lesson> { it.type.ordinal }.thenBy { it.categoryId }.thenBy(Lesson::order))
        .toList()

    fun observeWatchedLessonIds(userId: String): Flow<Set<String>> = callbackFlow {
        val registration = firestore.collection(USERS)
            .document(userId)
            .collection(LESSON_PROGRESS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapTo(mutableSetOf()) { document -> document.id }.orEmpty())
        }
        awaitClose(registration::remove)
    }

    fun observeBookmarkedLessonIds(userId: String): Flow<Set<String>> = callbackFlow {
        val registration = firestore.collection(USERS)
            .document(userId)
            .collection(LESSON_BOOKMARKS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapTo(mutableSetOf()) { document -> document.id }.orEmpty())
            }
        awaitClose(registration::remove)
    }

    suspend fun markAsWatched(userId: String, lessonId: String) {
        require(userId.isNotBlank()) { "A user ID is required to save lesson progress." }
        require(lessonId.isNotBlank()) { "A lesson ID is required to save lesson progress." }

        val lesson = firestore.collection(LESSONS).document(lessonId).get().await()
        require(lesson.exists()) { "Lesson '$lessonId' does not exist in Firestore." }

        val userRef = firestore.collection(USERS).document(userId)
        val progressRef = userRef.collection(LESSON_PROGRESS).document(lessonId)
        val batch = firestore.batch()
        batch.set(
            progressRef,
            mapOf(
                "lessonId" to lessonId,
                "watched" to true,
                "watchedAt" to FieldValue.serverTimestamp(),
            ),
            SetOptions.merge(),
        )
        batch.set(
            userRef,
            mapOf("watchedLessonIds" to FieldValue.arrayUnion(lessonId)),
            SetOptions.merge(),
        )
        batch.commit().await()
    }

    suspend fun setBookmarked(
        userId: String,
        lessonId: String,
        bookmarked: Boolean,
    ) {
        require(userId.isNotBlank()) { "A user ID is required to save bookmarks." }
        require(lessonId.isNotBlank()) { "A lesson ID is required to save bookmarks." }

        val lesson = firestore.collection(LESSONS).document(lessonId).get().await()
        require(lesson.exists()) { "Lesson '$lessonId' does not exist in Firestore." }

        val userRef = firestore.collection(USERS).document(userId)
        val bookmarkRef = userRef.collection(LESSON_BOOKMARKS).document(lessonId)
        val batch = firestore.batch()
        if (bookmarked) {
            batch.set(
                bookmarkRef,
                mapOf(
                    "lessonId" to lessonId,
                    "bookmarked" to true,
                    "bookmarkedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            batch.set(
                userRef,
                mapOf("bookmarkedLessonIds" to FieldValue.arrayUnion(lessonId)),
                SetOptions.merge(),
            )
        } else {
            batch.delete(bookmarkRef)
            batch.set(
                userRef,
                mapOf("bookmarkedLessonIds" to FieldValue.arrayRemove(lessonId)),
                SetOptions.merge(),
            )
        }
        batch.commit().await()
    }

    private suspend fun getLessonsForLanguage(languageId: String): List<Lesson> {
        lessonCache[languageId]?.let { return it }
        return firestore.collection(LESSONS)
            .whereEqualTo(LANGUAGE_ID, languageId)
            .get()
            .await()
            .documents
            .map { document -> document.toLesson() }
            .also { lessons ->
                require(lessons.isNotEmpty()) { "Firestore does not contain lessons for '$languageId'." }
                Log.i(TAG, "Loaded ${lessons.size} lessons from Firestore.")
            }
            .also { lessons -> lessonCache[languageId] = lessons }
    }

    private fun DocumentSnapshot.toProgrammingLanguage() = ProgrammingLanguage(
        id = id,
        name = requiredString(NAME),
        description = getString(DESCRIPTION).orEmpty(),
        iconUrl = getString(ICON_URL),
        order = getLong(ORDER)?.toInt() ?: 0,
        isEnabled = getBoolean(IS_ENABLED) ?: true,
    )

    private fun DocumentSnapshot.toLessonCategory() = LessonCategory(
        id = id,
        languageId = requiredString(LANGUAGE_ID),
        name = requiredString(NAME),
        lessonCount = 0,
        description = getString(DESCRIPTION).orEmpty(),
        type = contentType(default = LessonContentType.TUTORIAL),
        iconUrl = getString(ICON_URL),
        order = getLong(ORDER)?.toInt() ?: 0,
    )

    private fun DocumentSnapshot.toLesson(): Lesson {
        val videoId = getString(VIDEO_ID).orEmpty()
        return Lesson(
            id = id,
            languageId = requiredString(LANGUAGE_ID),
            categoryId = requiredString(CATEGORY_ID),
            title = requiredString(TITLE),
            videoId = videoId,
            order = getLong(ORDER)?.toInt() ?: 0,
            durationSeconds = getLong(DURATION_SECONDS)?.toInt() ?: 0,
            description = getString(DESCRIPTION).orEmpty(),
            content = getString(CONTENT).orEmpty(),
            type = contentType(default = LessonContentType.VIDEO),
            sourceName = getString(SOURCE_NAME).orEmpty(),
            thumbnailUrl = getString(THUMBNAIL_URL)
                ?: videoId.takeIf(String::isNotBlank)
                    ?.let { id -> "https://img.youtube.com/vi/$id/hqdefault.jpg" },
        )
    }

    private fun DocumentSnapshot.requiredString(field: String): String =
        requireNotNull(getString(field)?.takeIf(String::isNotBlank)) {
            "Firestore document '$id' is missing '$field'."
        }

    private fun DocumentSnapshot.contentType(default: LessonContentType): LessonContentType =
        runCatching { LessonContentType.valueOf(getString(TYPE).orEmpty().uppercase()) }
            .getOrDefault(default)

    private companion object {
        const val PROGRAMMING_LANGUAGES = "programming_languages"
        const val LESSON_CATEGORIES = "lesson_categories"
        const val LESSONS = "lessons"
        const val USERS = "users"
        const val LESSON_PROGRESS = "lessonProgress"
        const val LESSON_BOOKMARKS = "lessonBookmarks"
        const val LANGUAGE_ID = "languageId"
        const val CATEGORY_ID = "categoryId"
        const val NAME = "name"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val CONTENT = "content"
        const val TYPE = "type"
        const val VIDEO_ID = "videoId"
        const val SOURCE_NAME = "sourceName"
        const val THUMBNAIL_URL = "thumbnailUrl"
        const val ICON_URL = "iconUrl"
        const val ORDER = "order"
        const val DURATION_SECONDS = "durationSeconds"
        const val IS_ENABLED = "isEnabled"
        const val TAG = "EzTechFirestore"
    }
}
