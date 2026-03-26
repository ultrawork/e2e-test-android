package com.ultrawork.notes.di

import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.model.Note
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Test DI module that replaces AppModule in instrumented tests.
 * Provides a FakeNotesRepository with the same 5 hardcoded notes
 * so existing UI tests (SC-001 to SC-003) continue to work.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideNotesRepository(): NotesRepository {
        return FakeNotesRepository()
    }
}

/**
 * Fake repository returning the same hardcoded notes as the original ViewModel.
 */
class FakeNotesRepository : NotesRepository {

    private val notes = mutableListOf(
        Note(id = 1, title = "Shopping List", content = "Milk, Eggs, Bread"),
        Note(id = 2, title = "Meeting Notes", content = "Discuss project timeline"),
        Note(id = 3, title = "Ideas", content = "New app features"),
        Note(id = 4, title = "Travel Plans", content = "Book flights and hotel"),
        Note(id = 5, title = "Work Tasks", content = "Complete documentation")
    )

    override suspend fun getNotes(): List<Note> = notes.toList()

    override suspend fun createNote(title: String, content: String): Note {
        val note = Note(
            id = (notes.maxOfOrNull { it.id } ?: 0) + 1,
            title = title,
            content = content
        )
        notes.add(note)
        return note
    }

    override suspend fun deleteNote(id: Long) {
        notes.removeAll { it.id == id }
    }
}
