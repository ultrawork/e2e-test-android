package com.ultrawork.notes.repository

import com.ultrawork.notes.model.Note

/**
 * Интерфейс репозитория заметок.
 */
interface NotesRepository {
    suspend fun getNotes(): Result<List<Note>>
    suspend fun createNote(title: String, content: String): Result<Note>
    suspend fun deleteNote(id: String): Result<Unit>
    suspend fun toggleFavorite(id: String): Result<Note>
}
