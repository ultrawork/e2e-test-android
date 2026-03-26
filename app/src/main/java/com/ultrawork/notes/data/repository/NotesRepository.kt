package com.ultrawork.notes.data.repository

import com.ultrawork.notes.model.Note

/**
 * Repository interface for notes data operations.
 */
interface NotesRepository {

    suspend fun getNotes(): List<Note>

    suspend fun createNote(title: String, content: String): Note

    suspend fun deleteNote(id: Long)
}
