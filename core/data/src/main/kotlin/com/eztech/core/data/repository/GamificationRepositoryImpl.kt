package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.BadgeCatalog
import com.eztech.core.domain.model.DailyLoginResult
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.domain.model.GamificationProgress
import com.eztech.core.domain.model.LeaderboardEntry
import com.eztech.core.domain.model.ProblemCompletion
import com.eztech.core.domain.model.computeLevel
import com.eztech.core.domain.repository.GamificationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

internal class GamificationRepositoryImpl(
    private val firestore: FirebaseFirestore,
) : GamificationRepository {

    override suspend fun awardExp(
        userId: String,
        amount: Int,
        reason: String,
    ): Resource<Unit> = runCatching {
        require(amount > 0) { "EXP amount must be positive." }
        require(reason.isNotBlank()) { "An idempotency reason is required." }
        firestore.runTransaction { transaction ->
            val userRef = firestore.collection(USERS).document(userId)
            val leaderboardRef = firestore.collection(LEADERBOARD).document(userId)
            val eventRef = userRef.collection(EXP_EVENTS).document(reason.sha256())
            val snapshot = transaction.get(userRef)
            val eventSnapshot = transaction.get(eventRef)
            check(snapshot.exists()) { "User profile not found." }

            if (eventSnapshot.exists()) return@runTransaction

            val newExp = snapshot.longValue("exp") + amount
            val newLevel = computeLevel(newExp)
            transaction.set(
                userRef,
                mapOf("exp" to newExp, "level" to newLevel),
                SetOptions.merge(),
            )
            transaction.set(
                leaderboardRef,
                snapshot.leaderboardFields(
                    totalExp = newExp,
                    level = newLevel,
                ),
                SetOptions.merge(),
            )
            transaction.set(
                eventRef,
                mapOf(
                    "amount" to amount,
                    "reason" to reason,
                    "createdAt" to Timestamp.now(),
                ),
            )
            Unit
        }.await()
        Unit
    }.fold(
        onSuccess = { Resource.Success(Unit) },
        onFailure = { error -> error.toResourceError() },
    )

    override suspend fun getBadges(userId: String): Resource<List<Badge>> = runCatching {
        firestore.collection(USERS)
            .document(userId)
            .collection(BADGES)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                BadgeCatalog.ALL.firstOrNull { badge -> badge.id == document.id }?.copy(
                    unlocked = document.getBoolean("unlocked") == true,
                    unlockedAt = document.getLong("unlockedAt"),
                )
            }
    }.fold(
        onSuccess = { Resource.Success(it) },
        onFailure = { error -> error.toResourceError() },
    )

    override suspend fun unlockBadge(
        userId: String,
        badgeId: String,
    ): Resource<Unit> = runCatching {
        require(BadgeCatalog.ALL.any { badge -> badge.id == badgeId }) {
            "Unknown badge '$badgeId'."
        }
        firestore.collection(USERS)
            .document(userId)
            .collection(BADGES)
            .document(badgeId)
            .set(
                mapOf(
                    "unlocked" to true,
                    "unlockedAt" to System.currentTimeMillis(),
                ),
                SetOptions.merge(),
            )
            .await()
    }.fold(
        onSuccess = { Resource.Success(Unit) },
        onFailure = { error -> error.toResourceError() },
    )

    override suspend fun recordProblemSolved(
        userId: String,
        problemId: String,
        difficulty: Difficulty,
        expReward: Int,
        solveDurationSeconds: Int,
    ): Resource<ProblemCompletion> = runCatching {
        firestore.runTransaction { transaction ->
            val userRef = firestore.collection(USERS).document(userId)
            val leaderboardRef = firestore.collection(LEADERBOARD).document(userId)
            val solveRef = userRef.collection(SOLVED_PROBLEMS).document(problemId)
            val snapshot = transaction.get(userRef)
            check(snapshot.exists()) { "User profile not found." }

            val solvedIds = snapshot.stringList("solvedProblemIds")
            val currentProgress = snapshot.toProgress()
            if (problemId in solvedIds) {
                ProblemCompletion(
                    firstSolve = false,
                    awardedExp = 0,
                    progress = currentProgress,
                )
            } else {
                val newExp = currentProgress.totalExp + expReward
                val newSolvedIds = solvedIds + problemId
                val newHardCount = currentProgress.hardSolvedCount +
                    if (difficulty == Difficulty.HARD) 1 else 0
                val progress = currentProgress.copy(
                    totalExp = newExp,
                    level = computeLevel(newExp),
                    solvedCount = newSolvedIds.size,
                    hardSolvedCount = newHardCount,
                )

                transaction.set(
                    userRef,
                    mapOf(
                        "exp" to progress.totalExp,
                        "level" to progress.level,
                        "solvedCount" to progress.solvedCount,
                        "hardSolvedCount" to progress.hardSolvedCount,
                        "solvedProblemIds" to newSolvedIds,
                    ),
                    SetOptions.merge(),
                )
                transaction.set(
                    solveRef,
                    mapOf(
                        "problemId" to problemId,
                        "difficulty" to difficulty.name,
                        "expAwarded" to expReward,
                        "solveDurationSeconds" to solveDurationSeconds,
                        "solvedAt" to Timestamp.now(),
                    ),
                )
                transaction.set(
                    leaderboardRef,
                    snapshot.leaderboardFields(
                        totalExp = progress.totalExp,
                        level = progress.level,
                        solvedCount = progress.solvedCount,
                        hardSolvedCount = progress.hardSolvedCount,
                    ),
                    SetOptions.merge(),
                )
                ProblemCompletion(
                    firstSolve = true,
                    awardedExp = expReward,
                    progress = progress,
                )
            }
        }.await()
    }.fold(
        onSuccess = { Resource.Success(it) },
        onFailure = { error -> error.toResourceError() },
    )

    override suspend fun recordDailyLogin(
        userId: String,
        today: String,
        yesterday: String,
        expReward: Int,
    ): Resource<DailyLoginResult> = runCatching {
        firestore.runTransaction { transaction ->
            val userRef = firestore.collection(USERS).document(userId)
            val leaderboardRef = firestore.collection(LEADERBOARD).document(userId)
            val eventRef = userRef.collection(EXP_EVENTS).document("daily_$today")
            val snapshot = transaction.get(userRef)
            check(snapshot.exists()) { "User profile not found." }

            val progress = snapshot.toProgress()
            val lastLogin = snapshot.getString("lastLoginDate").orEmpty()
            if (lastLogin == today) {
                DailyLoginResult(
                    firstLoginToday = false,
                    awardedExp = 0,
                    progress = progress,
                )
            } else {
                val newStreak = if (lastLogin == yesterday) progress.currentStreak + 1 else 1
                val newExp = progress.totalExp + expReward
                val updatedProgress = progress.copy(
                    totalExp = newExp,
                    level = computeLevel(newExp),
                    currentStreak = newStreak,
                )
                transaction.set(
                    userRef,
                    mapOf(
                        "exp" to updatedProgress.totalExp,
                        "level" to updatedProgress.level,
                        "lastLoginDate" to today,
                        "currentStreak" to updatedProgress.currentStreak,
                    ),
                    SetOptions.merge(),
                )
                transaction.set(
                    eventRef,
                    mapOf(
                        "amount" to expReward,
                        "reason" to "daily_login",
                        "createdAt" to Timestamp.now(),
                    ),
                )
                transaction.set(
                    leaderboardRef,
                    snapshot.leaderboardFields(
                        totalExp = updatedProgress.totalExp,
                        level = updatedProgress.level,
                        currentStreak = updatedProgress.currentStreak,
                    ),
                    SetOptions.merge(),
                )
                DailyLoginResult(
                    firstLoginToday = true,
                    awardedExp = expReward,
                    progress = updatedProgress,
                )
            }
        }.await()
    }.fold(
        onSuccess = { Resource.Success(it) },
        onFailure = { error -> error.toResourceError() },
    )

    override fun observeLeaderboard(): Flow<Resource<List<LeaderboardEntry>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = firestore.collection(LEADERBOARD)
            .orderBy("totalExp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(error.toResourceError())
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents.orEmpty().mapIndexed { index, document ->
                    document.toLeaderboardEntry(rank = index + 1)
                }
                trySend(Resource.Success(entries))
            }
        awaitClose(registration::remove)
    }

    override fun observeUserLeaderboardEntry(
        userId: String,
    ): Flow<Resource<LeaderboardEntry?>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = firestore.collection(LEADERBOARD)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(error.toResourceError())
                    return@addSnapshotListener
                }
                val entry = snapshot
                    ?.takeIf(DocumentSnapshot::exists)
                    ?.toLeaderboardEntry(rank = snapshot.getLong("rank")?.toInt() ?: 0)
                    ?.copy(isCurrentUser = true)
                trySend(Resource.Success(entry))
            }
        awaitClose(registration::remove)
    }

    private fun DocumentSnapshot.toProgress() = GamificationProgress(
        totalExp = longValue("exp"),
        level = computeLevel(longValue("exp")),
        solvedCount = longValue("solvedCount"),
        hardSolvedCount = longValue("hardSolvedCount"),
        currentStreak = longValue("currentStreak"),
        watchedLessonCount = stringList("watchedLessonIds").size,
    )

    private fun DocumentSnapshot.leaderboardFields(
        totalExp: Int,
        level: Int,
        solvedCount: Int = longValue("solvedCount"),
        hardSolvedCount: Int = longValue("hardSolvedCount"),
        currentStreak: Int = longValue("currentStreak"),
    ): Map<String, Any?> = mapOf(
        "displayName" to getString("name").orEmpty().ifBlank { "Anonymous" },
        "avatarUrl" to getString("avatarUrl"),
        "totalExp" to totalExp,
        "solvedCount" to solvedCount,
        "hardSolvedCount" to hardSolvedCount,
        "level" to level,
        "currentStreak" to currentStreak,
        "updatedAt" to Timestamp.now(),
    )

    private fun DocumentSnapshot.toLeaderboardEntry(rank: Int) = LeaderboardEntry(
        rank = rank,
        userId = id,
        displayName = getString("displayName") ?: "Anonymous",
        avatarUrl = getString("avatarUrl"),
        totalExp = longValue("totalExp"),
        solvedCount = longValue("solvedCount"),
        level = getLong("level")?.toInt() ?: 1,
        currentStreak = longValue("currentStreak"),
    )

    private fun DocumentSnapshot.longValue(field: String): Int =
        getLong(field)?.toInt() ?: 0

    private fun DocumentSnapshot.stringList(field: String): List<String> =
        (get(field) as? List<*>)?.filterIsInstance<String>().orEmpty()

    private fun Throwable.toResourceError() = Resource.Error(
        message = localizedMessage ?: "Gamification data is unavailable.",
        cause = this,
    )

    private fun String.sha256(): String = MessageDigest
        .getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(byte) }

    private companion object {
        const val USERS = "users"
        const val LEADERBOARD = "leaderboard"
        const val BADGES = "badges"
        const val SOLVED_PROBLEMS = "solvedProblems"
        const val EXP_EVENTS = "expEvents"
    }
}
