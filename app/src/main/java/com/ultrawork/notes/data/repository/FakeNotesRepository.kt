package com.ultrawork.notes.data.repository

import com.ultrawork.notes.model.Note
import java.time.Instant
import java.util.UUID

/**
 * In-memory fake implementation of [NotesRepository] for development without a backend.
 */
class FakeNotesRepository : NotesRepository {

    private val notes = mutableListOf<Note>()

    override suspend fun getNotes(): List<Note> = notes.toList()

    override suspend fun getNote(id: String): Note? = notes.find { it.id == id }

    override suspend fun createNote(note: Note): Note {
        val now = Instant.now().toString()
        val created = note.copy(
            id = note.id.ifBlank { UUID.randomUUID().toString() },
            createdAt = note.createdAt ?: now,
            updatedAt = note.updatedAt ?: now
        )
        notes.add(created)
        return created
    }

    override suspend fun updateNote(note: Note): Note {
        val index = notes.indexOfFirst { it.id == note.id }
        if (index == -1) throw NoSuchElementException("Note with id=${note.id} not found")
        val updated = note.copy(updatedAt = Instant.now().toString())
        notes[index] = updated
        return updated
    }

    override suspend fun deleteNote(id: String): Boolean {
        return notes.removeAll { it.id == id }
    }
}
