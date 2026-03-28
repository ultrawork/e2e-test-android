package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.model.Note

/** Интерфейс репозитория для работы с заметками. */
interface NotesRepository {
    suspend fun getNotes(): Result<List<Note>>
    suspend fun createNote(title: String, content: String): Result<Note>
    suspend fun deleteNote(id: String): Result<Unit>
}
