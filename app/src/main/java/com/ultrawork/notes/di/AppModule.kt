package com.ultrawork.notes.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Repository layer is temporarily provided via ServiceLocator, not Hilt.
    // TODO: Migrate Retrofit, Room database, and repositories to Hilt providers.
}
