package com.ultrawork.notes.di

import com.ultrawork.notes.data.repository.CategoryRepository
import com.ultrawork.notes.data.repository.NotesRepository
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
    fun provideCategoryRepository(): CategoryRepository =
        DefaultServiceLocator.provideCategoryRepository()

    @Provides
    @Singleton
    fun provideNotesRepository(): NotesRepository =
        DefaultServiceLocator.provideNotesRepository()
}
