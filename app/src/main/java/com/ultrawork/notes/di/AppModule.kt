package com.ultrawork.notes.di

import com.ultrawork.notes.BuildConfig
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
    fun provideServiceLocator(): DefaultServiceLocator =
        DefaultServiceLocator(BuildConfig.API_BASE_URL)

    @Provides
    @Singleton
    fun provideNotesRepository(locator: DefaultServiceLocator): NotesRepository =
        locator.provideNotesRepository()
}
