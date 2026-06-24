package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.User
import com.eztech.core.domain.model.computeLevel
import com.eztech.core.domain.repository.AuthRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

internal class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    override fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                ensureUserDocuments(firebaseUser)
            }
            trySend(firebaseUser?.toDomainUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): Resource<User> =
        runCatching {
            val firebaseUser = firebaseAuth
                .signInWithEmailAndPassword(email.trim(), password)
                .await()
                .user ?: error("Firebase did not return a user after login.")
            loadUser(firebaseUser)
        }.fold(
            onSuccess = { Resource.Success(it) },
            onFailure = { it.toResourceError() },
        )

    override suspend fun register(name: String, email: String, password: String): Resource<User> =
        runCatching {
            val cleanName = name.trim()
            val firebaseUser = firebaseAuth
                .createUserWithEmailAndPassword(email.trim(), password)
                .await()
                .user ?: error("Firebase did not return a user after registration.")

            firebaseUser.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(cleanName).build()
            ).await()

            val user = firebaseUser.toDomainUser(name = cleanName)

            // Write user profile doc
            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(user.toFirestoreMap())
                .await()

            // Create leaderboard entry
            firestore.collection(LEADERBOARD_COLLECTION)
                .document(user.uid)
                .set(user.toLeaderboardMap())
                .await()

            user
        }.fold(
            onSuccess = { Resource.Success(it) },
            onFailure = { it.toResourceError() },
        )

    override suspend fun sendPasswordReset(email: String): Resource<Unit> =
        runCatching {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Unit
        }.fold(
            onSuccess = { Resource.Success(Unit) },
            onFailure = { it.toResourceError() },
        )

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private fun ensureUserDocuments(firebaseUser: FirebaseUser) {
        val user = firebaseUser.toDomainUser(
            name = firebaseUser.displayName
                ?: firebaseUser.email
                ?: "EzTech Learner",
        )
        val userDocument = firestore.collection(USERS_COLLECTION).document(user.uid)
        userDocument.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                userDocument.set(user.toFirestoreMap())
            }
        }

        val leaderboardDocument = firestore.collection(LEADERBOARD_COLLECTION).document(user.uid)
        leaderboardDocument.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                leaderboardDocument.set(user.toLeaderboardMap())
            }
        }
    }

    private suspend fun loadUser(firebaseUser: FirebaseUser): User {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .get()
            .await()

        if (!snapshot.exists()) {
            val user = firebaseUser.toDomainUser(
                name = firebaseUser.displayName
                    ?: firebaseUser.email
                    ?: "EzTech Learner",
            )
            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(user.toFirestoreMap())
                .await()
            firestore.collection(LEADERBOARD_COLLECTION)
                .document(user.uid)
                .set(user.toLeaderboardMap())
                .await()
            return user
        }

        val exp = snapshot.getLong("exp")?.toInt() ?: 0
        return firebaseUser.toDomainUser(
            name = snapshot.getString("name") ?: firebaseUser.displayName.orEmpty(),
            avatarUrl = snapshot.getString("avatarUrl") ?: firebaseUser.photoUrl?.toString(),
            exp = exp,
            level = computeLevel(exp),
            solvedCount = snapshot.getLong("solvedCount")?.toInt() ?: 0,
            hardSolvedCount = snapshot.getLong("hardSolvedCount")?.toInt() ?: 0,
            currentStreak = snapshot.getLong("currentStreak")?.toInt() ?: 0,
            lastLoginDate = snapshot.getString("lastLoginDate") ?: "",
        )
    }

    private fun FirebaseUser.toDomainUser(
        name: String = displayName.orEmpty(),
        avatarUrl: String? = photoUrl?.toString(),
        exp: Int = 0,
        level: Int = 1,
        solvedCount: Int = 0,
        hardSolvedCount: Int = 0,
        currentStreak: Int = 0,
        lastLoginDate: String = "",
    ) = User(
        uid = uid,
        name = name,
        email = email.orEmpty(),
        avatarUrl = avatarUrl,
        exp = exp,
        level = level,
        solvedCount = solvedCount,
        hardSolvedCount = hardSolvedCount,
        currentStreak = currentStreak,
        lastLoginDate = lastLoginDate,
    )

    private fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "avatarUrl" to avatarUrl,
        "exp" to 0,
        "level" to 1,
        "solvedCount" to 0,
        "hardSolvedCount" to 0,
        "solvedProblemIds" to emptyList<String>(),
        "watchedLessonIds" to emptyList<String>(),
        "currentStreak" to 0,
        "lastLoginDate" to "",
        "rank" to 0,
    )

    private fun User.toLeaderboardMap(): Map<String, Any?> = mapOf(
        "displayName" to name,
        "avatarUrl" to avatarUrl,
        "totalExp" to 0,
        "solvedCount" to 0,
        "hardSolvedCount" to 0,
        "level" to 1,
        "currentStreak" to 0,
        "updatedAt" to Timestamp.now(),
    )

    private fun Throwable.toResourceError() = Resource.Error(
        message = localizedMessage ?: "Authentication failed. Please try again.",
        cause = this,
    )

    private companion object {
        const val USERS_COLLECTION = "users"
        const val LEADERBOARD_COLLECTION = "leaderboard"
    }
}
