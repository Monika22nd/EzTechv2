package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.User
import com.eztech.core.domain.repository.AuthRepository
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
            trySend(auth.currentUser?.toDomainUser())
        }

        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): Resource<User> =
        runCatching {
            val firebaseUser = firebaseAuth
                .signInWithEmailAndPassword(email.trim(), password)
                .await()
                .user
                ?: error("Firebase did not return a user after login.")

            loadUser(firebaseUser)
        }.fold(
            onSuccess = { user -> Resource.Success(user) },
            onFailure = { error -> error.toResourceError() },
        )

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): Resource<User> = runCatching {
        val cleanName = name.trim()
        val firebaseUser = firebaseAuth
            .createUserWithEmailAndPassword(email.trim(), password)
            .await()
            .user
            ?: error("Firebase did not return a user after registration.")

        firebaseUser.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build(),
        ).await()

        val user = firebaseUser.toDomainUser(name = cleanName)
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(user.toFirestoreMap())
            .await()
        user
    }.fold(
        onSuccess = { user -> Resource.Success(user) },
        onFailure = { error -> error.toResourceError() },
    )

    override suspend fun sendPasswordReset(email: String): Resource<Unit> =
        runCatching {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Unit
        }.fold(
            onSuccess = { Resource.Success(Unit) },
            onFailure = { error -> error.toResourceError() },
        )

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    private suspend fun loadUser(firebaseUser: FirebaseUser): User {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .get()
            .await()

        return firebaseUser.toDomainUser(
            name = snapshot.getString("name") ?: firebaseUser.displayName.orEmpty(),
            avatarUrl = snapshot.getString("avatarUrl") ?: firebaseUser.photoUrl?.toString(),
            exp = snapshot.getLong("exp")?.toInt() ?: 0,
            level = snapshot.getLong("level")?.toInt() ?: 1,
        )
    }

    private fun FirebaseUser.toDomainUser(
        name: String = displayName.orEmpty(),
        avatarUrl: String? = photoUrl?.toString(),
        exp: Int = 0,
        level: Int = 1,
    ) = User(
        uid = uid,
        name = name,
        email = email.orEmpty(),
        avatarUrl = avatarUrl,
        exp = exp,
        level = level,
    )

    private fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "avatarUrl" to avatarUrl,
        "exp" to exp,
        "level" to level,
        "badges" to emptyList<String>(),
    )

    private fun Throwable.toResourceError() = Resource.Error(
        message = localizedMessage ?: "Authentication failed. Please try again.",
        cause = this,
    )

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
