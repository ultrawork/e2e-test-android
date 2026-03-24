package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.model.Note
import java.util.Date

/**
 * In-memory [NotesRepository] implementation used as a fallback
 * when the network backend is unavailable.
 */
class FakeNotesRepository : NotesRepository {

    private val notes = mutableListOf(
        Note(id = 1, title = "Shopping List", content = "Milk, Eggs, Bread", createdAt = Date(), updatedAt = Date()),
        Note(id = 2, title = "Meeting Notes", content = "Discuss project timeline", createdAt = Date(), updatedAt = Date()),
        Note(id = 3, title = "Ideas", content = "New app features", createdAt = Date(), updatedAt = Date()),
        Note(id = 4, title = "Travel Plans", content = "Book flights and hotel", createdAt = Date(), updatedAt = Date()),
        Note(id = 5, title = "Work Tasks", content = "Complete documentation", createdAt = Date(), updatedAt = Date())
    )

    override suspend fun getNotes(): Result<List<Note>> =
        Result.success(notes.toList())

    override suspend fun createNote(request: CreateNoteRequest): Result<Note> {
        val note = Note(
            id = (notes.maxOfOrNull { it.id } ?: 0) + 1,
            title = request.title,
            content = request.content,
            createdAt = Date(),
            updatedAt = Date()
        )
        notes.add(note)
        return Result.success(note)
    }

    override suspend fun deleteNote(id: Long): Result<Unit> {
        notes.removeAll { it.id == id }
        return Result.success(Unit)
    }

    override suspend fun toggleFavorite(id: Long): Result<Note> {
        val index = notes.indexOfFirst { it.id == id }
        if (index == -1) return Result.failure(NoSuchElementException("Note not found"))
        val updated = notes[index].copy(isFavorited = !notes[index].isFavorited, updatedAt = Date())
        notes[index] = updated
        return Result.success(updated)
    }
}
