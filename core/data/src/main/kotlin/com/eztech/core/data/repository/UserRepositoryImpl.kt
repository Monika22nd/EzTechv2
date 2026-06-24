package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.User
import com.eztech.core.domain.model.computeLevel
import com.eztech.core.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

internal class UserRepositoryImpl(
    private val firestore: FirebaseFirestore,
) : UserRepository {

    override fun observeUserProfile(userId: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading)
        var reg: ListenerRegistration? = null
        reg = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to observe profile"))
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    trySend(Resource.Error("User profile not found"))
                    return@addSnapshotListener
                }
                val exp = snapshot.getLong("exp")?.toInt() ?: 0
                val user = User(
                    uid = userId,
                    name = snapshot.getString("name") ?: "",
                    email = snapshot.getString("email") ?: "",
                    avatarUrl = snapshot.getString("avatarUrl"),
                    exp = exp,
                    level = computeLevel(exp),
                    solvedCount = snapshot.getLong("solvedCount")?.toInt() ?: 0,
                    solvedProblemIds = (snapshot.get("solvedProblemIds") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList(),
                    watchedLessonIds = (snapshot.get("watchedLessonIds") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList(),
                    currentStreak = snapshot.getLong("currentStreak")?.toInt() ?: 0,
                    lastLoginDate = snapshot.getString("lastLoginDate") ?: "",
                    rank = snapshot.getLong("rank")?.toInt() ?: 0,
                )
                trySend(Resource.Success(user))
            }
        awaitClose { reg?.remove() }
    }

    override suspend fun updateProfile(userId: String, name: String): Resource<Unit> =
        runCatching {
            firestore.collection("users").document(userId)
                .update("name", name.trim())
                .await()
            // Sync to leaderboard
            firestore.collection("leaderboard").document(userId)
                .update("displayName", name.trim())
                .await()
            Resource.Success(Unit)
        }.getOrElse { Resource.Error(it.message ?: "Update failed") }

    override suspend fun updateAvatar(userId: String, avatarBytes: ByteArray): Resource<String> =
        Resource.Error("Avatar upload requires Firebase Storage — implement in Phase 6")
}
