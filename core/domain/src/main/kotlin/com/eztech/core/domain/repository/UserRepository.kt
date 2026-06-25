package com.eztech.core.domain.repository

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUserProfile(userId: String): Flow<Resource<User>>

    suspend fun updateProfile(
        userId: String,
        name: String,
    ): Resource<Unit>

    suspend fun updateAvatarUrl(
        userId: String,
        avatarUrl: String,
    ): Resource<String>
}
