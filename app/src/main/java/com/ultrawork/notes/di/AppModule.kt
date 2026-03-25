package com.ultrawork.notes.di

import android.content.Context
import com.ultrawork.notes.data.repository.NotesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDefaultServiceLocator(
        @ApplicationContext context: Context
    ): DefaultServiceLocator = DefaultServiceLocator(context)

    @Provides
    @Singleton
    fun provideNotesRepository(
        serviceLocator: DefaultServiceLocator
    ): NotesRepository = serviceLocator.provideNotesRepository()
}
