package com.ultrawork.notes.data.repository.fake

import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.model.Note
import java.util.UUID

/**
 * Fake implementation of [NotesRepository] for unit and UI tests.
 *
 * @param initialNotes optional seed data; when null, generates 5 sample notes with UUID ids.
 */
class FakeNotesRepository(initialNotes: List<Note>? = null) : NotesRepository {

    private val notes = mutableMapOf<String, Note>()

    init {
        val seedNotes = initialNotes ?: listOf(
            Note(
                id = UUID.randomUUID().toString(),
                title = "Shopping List",
                content = "Milk, Eggs, Bread"
            ),
            Note(
                id = UUID.randomUUID().toString(),
                title = "Meeting Notes",
                content = "Discuss project timeline"
            ),
            Note(
                id = UUID.randomUUID().toString(),
                title = "Ideas",
                content = "New app features"
            ),
            Note(
                id = UUID.randomUUID().toString(),
                title = "Travel Plans",
                content = "Book flights and hotel"
            ),
            Note(
                id = UUID.randomUUID().toString(),
                title = "Work Tasks",
                content = "Complete documentation"
            )
        )
        seedNotes.forEach { notes[it.id] = it }
    }

    override suspend fun getNotes(favoritesOnly: Boolean): List<Note> {
        return if (favoritesOnly) {
            notes.values.filter { it.isFavorited }
        } else {
            notes.values.toList()
        }
    }

    override suspend fun toggleFavorite(id: String): Note {
        val note = notes[id] ?: throw NoSuchElementException("Note with id $id not found")
        val updated = note.copy(isFavorited = !note.isFavorited)
        notes[id] = updated
        return updated
    }
}
