package com.eztech.core.domain.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<User?>

    suspend fun login(
        email: String,
        password: String,
    ): Resource<User>

    suspend fun register(
        name: String,
        email: String,
        password: String,
    ): Resource<User>

    suspend fun sendPasswordReset(email: String): Resource<Unit>

    suspend fun logout()
}
