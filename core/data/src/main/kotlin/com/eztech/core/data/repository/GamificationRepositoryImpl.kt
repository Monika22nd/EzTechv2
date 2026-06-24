package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.BadgeCatalog
import com.eztech.core.domain.model.BadgeRarity
import com.eztech.core.domain.model.BadgeRequirement
import com.eztech.core.domain.model.BadgeRequirementType
import com.eztech.core.domain.model.LeaderboardEntry
import com.eztech.core.domain.model.computeLevel
import com.eztech.core.domain.repository.GamificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

internal class GamificationRepositoryImpl(
    private val firestore: FirebaseFirestore,
) : GamificationRepository {

    // ── EXP ────────────────────────────────────────────────────────────────

    override suspend fun awardExp(
        userId: String,
        amount: Int,
        reason: String,
    ): Resource<Unit> = runCatching {
        firestore.runTransaction { tx ->
            val userRef = firestore.collection("users").document(userId)
            val snapshot = tx.get(userRef)
            val currentExp = snapshot.getLong("exp")?.toInt() ?: 0
            val newExp = currentExp + amount
            val newLevel = computeLevel(newExp)
            tx.update(
                userRef,
                mapOf(
                    "exp" to newExp,
                    "level" to newLevel,
                ),
            )
            // Also update leaderboard denormalized doc
            val leaderRef = firestore.collection("leaderboard").document(userId)
            tx.set(
                leaderRef,
                mapOf(
                    "totalExp" to newExp,
                    "level" to newLevel,
                    "updatedAt" to com.google.firebase.Timestamp.now(),
                ),
                com.google.firebase.firestore.SetOptions.merge(),
            )
        }.await()
        Resource.Success(Unit)
    }.getOrElse { Resource.Error(it.message ?: "Failed to award EXP") }

    // ── Badges ─────────────────────────────────────────────────────────────

    override suspend fun getBadges(userId: String): Resource<List<Badge>> = runCatching {
        val snap = firestore.collection("users")
            .document(userId)
            .collection("badges")
            .get()
            .await()

        val badges = snap.documents.mapNotNull { doc ->
            val catalogBadge = BadgeCatalog.ALL.find { it.id == doc.id } ?: return@mapNotNull null
            catalogBadge.copy(
                unlocked = doc.getBoolean("unlocked") ?: false,
                unlockedAt = doc.getLong("unlockedAt"),
            )
        }
        Resource.Success(badges)
    }.getOrElse { Resource.Error(it.message ?: "Failed to fetch badges") }

    override suspend fun unlockBadge(
        userId: String,
        badgeId: String,
    ): Resource<Unit> = runCatching {
        firestore.collection("users")
            .document(userId)
            .collection("badges")
            .document(badgeId)
            .set(
                mapOf(
                    "unlocked" to true,
                    "unlockedAt" to System.currentTimeMillis(),
                ),
            )
            .await()
        Resource.Success(Unit)
    }.getOrElse { Resource.Error(it.message ?: "Failed to unlock badge") }

    // ── Daily Login ────────────────────────────────────────────────────────

    override suspend fun recordDailyLogin(
        userId: String,
        today: String,
    ): Resource<Unit> = runCatching {
        firestore.runTransaction { tx ->
            val userRef = firestore.collection("users").document(userId)
            val snap = tx.get(userRef)
            val lastLogin = snap.getString("lastLoginDate") ?: ""
            val prevStreak = snap.getLong("currentStreak")?.toInt() ?: 0

            // Check if yesterday → continue streak, else reset
            val yesterday = java.time.LocalDate.now().minusDays(1).toString()
            val newStreak = if (lastLogin == yesterday) prevStreak + 1 else 1

            tx.update(
                userRef,
                mapOf(
                    "lastLoginDate" to today,
                    "currentStreak" to newStreak,
                ),
            )
        }.await()
        Resource.Success(Unit)
    }.getOrElse { Resource.Error(it.message ?: "Failed to record daily login") }

    // ── Leaderboard ────────────────────────────────────────────────────────

    override fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = firestore.collection("leaderboard")
            .orderBy("totalExp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val entries = snapshot.documents.mapIndexedNotNull { index, doc ->
                    LeaderboardEntry(
                        rank = index + 1,
                        userId = doc.id,
                        displayName = doc.getString("displayName") ?: "Anonymous",
                        avatarUrl = doc.getString("avatarUrl"),
                        totalExp = doc.getLong("totalExp")?.toInt() ?: 0,
                        solvedCount = doc.getLong("solvedCount")?.toInt() ?: 0,
                        level = doc.getLong("level")?.toInt() ?: 1,
                        currentStreak = doc.getLong("currentStreak")?.toInt() ?: 0,
                    )
                }
                trySend(entries)
            }
        awaitClose { registration?.remove() }
    }

    override fun observeUserLeaderboardEntry(userId: String): Flow<LeaderboardEntry?> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = firestore.collection("leaderboard")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val entry = if (snapshot.exists()) {
                    LeaderboardEntry(
                        rank = snapshot.getLong("rank")?.toInt() ?: 0,
                        userId = userId,
                        displayName = snapshot.getString("displayName") ?: "Anonymous",
                        avatarUrl = snapshot.getString("avatarUrl"),
                        totalExp = snapshot.getLong("totalExp")?.toInt() ?: 0,
                        solvedCount = snapshot.getLong("solvedCount")?.toInt() ?: 0,
                        level = snapshot.getLong("level")?.toInt() ?: 1,
                        currentStreak = snapshot.getLong("currentStreak")?.toInt() ?: 0,
                        isCurrentUser = true,
                    )
                } else null
                trySend(entry)
            }
        awaitClose { registration?.remove() }
    }
}
