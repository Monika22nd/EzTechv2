package com.eztech.core.data.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.User
import com.eztech.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class UnconfiguredAuthRepository : AuthRepository {
    override fun observeCurrentUser(): Flow<User?> = flowOf(null)

    override suspend fun login(email: String, password: String): Resource<User> = configurationError()

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): Resource<User> = configurationError()

    override suspend fun sendPasswordReset(email: String): Resource<Unit> = configurationError()

    override suspend fun logout() = Unit

    private fun configurationError() = Resource.Error(
        message = "Firebase is not configured. Add google-services.json to the app module.",
    )
}
