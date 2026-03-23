package com.ultrawork.notes.di

import com.ultrawork.notes.data.repository.FakeNotesRepository
import com.ultrawork.notes.data.repository.NotesRepository

/**
 * Service locator that provides repository instances based on configuration.
 *
 * When [apiBaseUrl] is blank, returns [FakeNotesRepository].
 * A real API-backed repository will be provided in a future iteration.
 */
class DefaultServiceLocator {

    fun provideNotesRepository(apiBaseUrl: String): NotesRepository =
        if (apiBaseUrl.isBlank()) {
            FakeNotesRepository()
        } else {
            // Placeholder until real Retrofit-backed repository is implemented
            FakeNotesRepository()
        }
}
