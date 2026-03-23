package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.model.Note

/**
 * Network-backed implementation of [NotesRepository] using Retrofit [ApiService].
 * All calls are wrapped in [runCatching] to return [Result].
 */
class NotesRepositoryImpl(private val apiService: ApiService) : NotesRepository {

    override suspend fun getNotes(): Result<List<Note>> =
        runCatching { apiService.getNotes() }

    override suspend fun createNote(request: CreateNoteRequest): Result<Note> =
        runCatching { apiService.createNote(request) }

    override suspend fun deleteNote(id: String): Result<Unit> =
        runCatching { apiService.deleteNote(id) }

    override suspend fun toggleFavorite(id: String): Result<Note> =
        runCatching { apiService.toggleFavorite(id) }
}
