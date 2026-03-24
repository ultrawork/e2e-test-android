package com.ultrawork.notes.repository

import com.ultrawork.notes.model.Note
import com.ultrawork.notes.network.ApiService
import com.ultrawork.notes.network.CreateNoteRequest

/**
 * Реализация репозитория заметок через Retrofit API.
 */
class NotesRepositoryImpl(
    private val apiService: ApiService
) : NotesRepository {

    override suspend fun getNotes(): Result<List<Note>> = runCatching {
        apiService.getNotes()
    }

    override suspend fun createNote(title: String, content: String): Result<Note> = runCatching {
        apiService.createNote(CreateNoteRequest(title = title, content = content))
    }

    override suspend fun deleteNote(id: String): Result<Unit> = runCatching {
        apiService.deleteNote(id)
    }

    override suspend fun toggleFavorite(id: String): Result<Note> = runCatching {
        apiService.toggleFavorite(id)
    }
}
