package com.eztech.core.data.repository

import android.net.Uri
import com.eztech.core.common.Resource
import com.eztech.core.domain.model.User
import com.eztech.core.domain.model.computeLevel
import com.eztech.core.domain.repository.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

internal class UserRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
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
                    ensureProfileExists(userId)
                    trySend(Resource.Loading)
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
                    hardSolvedCount = snapshot.getLong("hardSolvedCount")?.toInt() ?: 0,
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
                .set(mapOf("name" to name.trim()), SetOptions.merge())
                .await()
            firestore.collection("leaderboard").document(userId)
                .set(
                    mapOf("displayName" to name.trim()),
                    SetOptions.merge(),
                )
                .await()
            Resource.Success(Unit)
        }.getOrElse { Resource.Error(it.message ?: "Update failed") }

    override suspend fun updateAvatarUrl(userId: String, avatarUrl: String): Resource<String> =
        runCatching {
            require(userId.isNotBlank()) { "User ID is required." }
            val normalizedUrl = avatarUrl.trim()
            require(
                normalizedUrl.isBlank() ||
                    normalizedUrl.startsWith("https://") ||
                    normalizedUrl.startsWith("http://"),
            ) {
                "Avatar URL must start with http:// or https://."
            }

            val storedAvatarUrl = normalizedUrl.ifBlank { null }
            firestore.collection("users")
                .document(userId)
                .set(mapOf("avatarUrl" to storedAvatarUrl), SetOptions.merge())
                .await()
            firestore.collection("leaderboard")
                .document(userId)
                .set(mapOf("avatarUrl" to storedAvatarUrl), SetOptions.merge())
                .await()

            firebaseAuth.currentUser
                ?.takeIf { user -> user.uid == userId }
                ?.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setPhotoUri(storedAvatarUrl?.let(Uri::parse))
                        .build(),
                )
                ?.await()

            normalizedUrl
        }.fold(
            onSuccess = { avatarUrl -> Resource.Success(avatarUrl) },
            onFailure = { error ->
                Resource.Error(error.message ?: "Avatar update failed.", error)
            },
        )

    private fun ensureProfileExists(userId: String) {
        val firebaseUser = firebaseAuth.currentUser?.takeIf { user -> user.uid == userId }
        val displayName = firebaseUser?.displayName
            ?: firebaseUser?.email
            ?: "EzTech Learner"
        val avatarUrl = firebaseUser?.photoUrl?.toString()

        firestore.collection("users")
            .document(userId)
            .set(
                mapOf(
                    "uid" to userId,
                    "name" to displayName,
                    "email" to (firebaseUser?.email ?: ""),
                    "avatarUrl" to avatarUrl,
                    "exp" to 0,
                    "level" to 1,
                    "solvedCount" to 0,
                    "hardSolvedCount" to 0,
                    "solvedProblemIds" to emptyList<String>(),
                    "watchedLessonIds" to emptyList<String>(),
                    "bookmarkedLessonIds" to emptyList<String>(),
                    "currentStreak" to 0,
                    "lastLoginDate" to "",
                    "rank" to 0,
                ),
                SetOptions.merge(),
            )

        firestore.collection("leaderboard")
            .document(userId)
            .set(
                mapOf(
                    "displayName" to displayName,
                    "avatarUrl" to avatarUrl,
                    "totalExp" to 0,
                    "solvedCount" to 0,
                    "hardSolvedCount" to 0,
                    "level" to 1,
                    "currentStreak" to 0,
                    "updatedAt" to Timestamp.now(),
                ),
                SetOptions.merge(),
            )
    }

}
