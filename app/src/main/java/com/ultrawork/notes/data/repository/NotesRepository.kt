package com.ultrawork.notes.data.repository

import com.ultrawork.notes.model.Note

/**
 * Контракт репозитория для работы с заметками.
 */
interface NotesRepository {
    suspend fun getNotes(categoryId: String? = null): Result<List<Note>>
    suspend fun getNote(id: Long): Result<Note>
    suspend fun create(note: Note): Result<Note>
    suspend fun update(note: Note): Result<Note>
    suspend fun delete(id: Long): Result<Unit>
}
