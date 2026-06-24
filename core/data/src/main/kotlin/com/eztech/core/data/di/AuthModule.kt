package com.eztech.core.data.di

import android.content.Context
import com.eztech.core.data.repository.FirebaseAuthRepository
import com.eztech.core.data.repository.UnconfiguredAuthRepository
import com.eztech.core.domain.repository.AuthRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
    ): AuthRepository {
        val firebaseApp = FirebaseApp.getApps(context).firstOrNull()
            ?: return UnconfiguredAuthRepository()

        return FirebaseAuthRepository(
            firebaseAuth = FirebaseAuth.getInstance(firebaseApp),
            firestore = FirebaseFirestore.getInstance(firebaseApp),
        )
    }
}
