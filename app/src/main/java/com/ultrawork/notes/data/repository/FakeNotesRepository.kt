package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.model.Note
import java.time.Instant
import java.util.UUID

/**
 * In-memory fake implementation of [NotesRepository] for development without a backend.
 */
class FakeNotesRepository : NotesRepository {

    private val notes = mutableListOf<Note>()

    override suspend fun getNotes(): Result<List<Note>> =
        runCatching { notes.toList() }

    override suspend fun createNote(request: CreateNoteRequest): Result<Note> =
        runCatching {
            val now = Instant.now().toString()
            val created = Note(
                id = UUID.randomUUID().toString(),
                title = request.title,
                content = request.content,
                createdAt = now,
                updatedAt = now
            )
            notes.add(created)
            created
        }

    override suspend fun deleteNote(id: String): Result<Unit> =
        runCatching {
            notes.removeAll { it.id == id }
            Unit
        }

    override suspend fun toggleFavorite(id: String): Result<Note> =
        runCatching {
            val index = notes.indexOfFirst { it.id == id }
            if (index == -1) throw NoSuchElementException("Note with id=$id not found")
            val note = notes[index]
            val toggled = note.copy(
                isFavorited = !note.isFavorited,
                updatedAt = Instant.now().toString()
            )
            notes[index] = toggled
            toggled
        }
}
