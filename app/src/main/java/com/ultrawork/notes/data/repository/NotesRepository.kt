package com.ultrawork.notes.data.repository

import com.ultrawork.notes.model.Note

/**
 * Repository interface for notes CRUD operations.
 */
interface NotesRepository {
    suspend fun getNotes(): List<Note>
    suspend fun getNote(id: String): Note?
    suspend fun createNote(note: Note): Note
    suspend fun updateNote(note: Note): Note
    suspend fun deleteNote(id: String): Boolean
}
