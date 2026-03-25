package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.remote.dto.CreateNoteRequest
import com.ultrawork.notes.data.remote.dto.toNote
import com.ultrawork.notes.model.Note

/**
 * Production implementation of [NotesRepository] backed by Retrofit [ApiService].
 */
class NotesRepositoryImpl(
    private val apiService: ApiService
) : NotesRepository {

    override suspend fun getNotes(): Result<List<Note>> = runCatching {
        apiService.getNotes().map { it.toNote() }
    }

    override suspend fun createNote(title: String): Result<Note> = runCatching {
        apiService.createNote(CreateNoteRequest(title = title, content = title)).toNote()
    }

    override suspend fun deleteNote(id: String): Result<Unit> = runCatching {
        apiService.deleteNote(id)
        Unit
    }
}
