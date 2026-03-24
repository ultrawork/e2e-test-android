package com.ultrawork.notes.repository

import com.ultrawork.notes.model.Note
import java.util.UUID

/**
 * Фейковый репозиторий заметок для fallback при недоступном backend.
 */
class FakeNotesRepository : NotesRepository {

    private val notes = mutableListOf(
        Note(
            id = "fake-1",
            title = "Shopping List",
            content = "Milk, Eggs, Bread",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        ),
        Note(
            id = "fake-2",
            title = "Meeting Notes",
            content = "Discuss project timeline",
            createdAt = "2024-01-02T00:00:00Z",
            updatedAt = "2024-01-02T00:00:00Z"
        )
    )

    override suspend fun getNotes(): Result<List<Note>> =
        Result.success(notes.toList())

    override suspend fun createNote(title: String, content: String): Result<Note> {
        val note = Note(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        notes.add(note)
        return Result.success(note)
    }

    override suspend fun deleteNote(id: String): Result<Unit> {
        notes.removeAll { it.id == id }
        return Result.success(Unit)
    }

    override suspend fun toggleFavorite(id: String): Result<Note> {
        val index = notes.indexOfFirst { it.id == id }
        if (index == -1) return Result.failure(NoSuchElementException("Note not found: $id"))
        val updated = notes[index].copy(isFavorited = !notes[index].isFavorited)
        notes[index] = updated
        return Result.success(updated)
    }
}
