package com.ultrawork.notes.data.repository

import com.ultrawork.notes.model.Note
import java.util.Date
import java.util.UUID

/**
 * Fake implementation of [NotesRepository] for fallback and testing.
 */
class FakeNotesRepository : NotesRepository {

    private val notes = mutableListOf(
        Note(id = "1", title = "Shopping List", content = "Milk, Eggs, Bread", createdAt = Date(), updatedAt = Date()),
        Note(id = "2", title = "Meeting Notes", content = "Discuss project timeline", createdAt = Date(), updatedAt = Date()),
        Note(id = "3", title = "Ideas", content = "New app features", createdAt = Date(), updatedAt = Date()),
        Note(id = "4", title = "Travel Plans", content = "Book flights and hotel", createdAt = Date(), updatedAt = Date()),
        Note(id = "5", title = "Work Tasks", content = "Complete documentation", createdAt = Date(), updatedAt = Date())
    )

    override suspend fun getNotes(): Result<List<Note>> = Result.success(notes.toList())

    override suspend fun createNote(title: String): Result<Note> {
        val note = Note(
            id = UUID.randomUUID().toString(),
            title = title,
            content = title,
            createdAt = Date(),
            updatedAt = Date()
        )
        notes.add(note)
        return Result.success(note)
    }

    override suspend fun deleteNote(id: String): Result<Unit> {
        notes.removeAll { it.id == id }
        return Result.success(Unit)
    }
}
