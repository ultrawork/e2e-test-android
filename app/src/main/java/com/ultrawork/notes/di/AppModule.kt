package com.ultrawork.notes.di

import com.ultrawork.notes.NotesApp
import com.ultrawork.notes.data.repository.FakeNotesRepository
import com.ultrawork.notes.data.repository.NotesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that bridges [DefaultServiceLocator] into the Dagger graph.
 * Provides [NotesRepository] backed by the network layer when available,
 * falling back to [FakeNotesRepository] otherwise.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNotesRepository(): NotesRepository {
        return NotesApp.serviceLocator.notesRepository
            ?: FakeNotesRepository()
    }
}
