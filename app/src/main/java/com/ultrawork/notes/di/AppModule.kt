package com.ultrawork.notes.di

import com.ultrawork.notes.ServiceLocator
import com.ultrawork.notes.repository.NotesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNotesRepository(): NotesRepository = ServiceLocator.notesRepository
}
