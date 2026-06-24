package com.eztech.core.data.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(
        @ApplicationContext context: Context,
    ): FirebaseFirestore {
        val app = FirebaseApp.getApps(context).firstOrNull()
            ?: return FirebaseFirestore.getInstance()
        return FirebaseFirestore.getInstance(app)
    }
}
