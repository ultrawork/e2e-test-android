package com.ultrawork.notes.data.repository

import com.ultrawork.notes.model.Note

/**
 * Контракт репозитория заметок.
 */
interface NotesRepository {
    suspend fun getNotes(): Result<List<Note>>
    suspend fun createNote(title: String, content: String): Result<Note>
    suspend fun updateNote(id: String, title: String, content: String): Result<Note>
    suspend fun deleteNote(id: String): Result<Unit>
}
