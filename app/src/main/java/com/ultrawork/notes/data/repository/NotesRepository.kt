package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.model.Note

/**
 * Repository interface for notes operations.
 */
interface NotesRepository {
    suspend fun getNotes(): Result<List<Note>>
    suspend fun createNote(request: CreateNoteRequest): Result<Note>
    suspend fun deleteNote(id: String): Result<Unit>
    suspend fun toggleFavorite(id: String): Result<Note>
}
