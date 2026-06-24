package com.eztech.core.data.di

import com.eztech.core.data.repository.UserRepositoryImpl
import com.eztech.core.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
    ): UserRepository = UserRepositoryImpl(
        firestore = firestore,
        firebaseAuth = firebaseAuth,
    )
}
