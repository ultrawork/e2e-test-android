package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.data.remote.UpdateNoteRequest
import com.ultrawork.notes.model.Note
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация репозитория заметок через Retrofit ApiService.
 */
@Singleton
class NotesRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : NotesRepository {

    override suspend fun getNotes(): Result<List<Note>> = runCatching {
        apiService.getNotes()
    }

    override suspend fun createNote(title: String, content: String): Result<Note> = runCatching {
        apiService.createNote(CreateNoteRequest(title, content))
    }

    override suspend fun updateNote(id: String, title: String, content: String): Result<Note> = runCatching {
        apiService.updateNote(id, UpdateNoteRequest(title, content))
    }

    override suspend fun deleteNote(id: String): Result<Unit> = runCatching {
        apiService.deleteNote(id)
        Unit
    }
}
