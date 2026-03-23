package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.model.Note

/**
 * Network-backed implementation of [NotesRepository] using Retrofit [ApiService].
 */
class NotesRepositoryImpl(private val apiService: ApiService) : NotesRepository {

    override suspend fun getNotes(): List<Note> =
        apiService.getNotes()

    override suspend fun getNote(id: String): Note? =
        apiService.getNotes().find { it.id == id }

    override suspend fun createNote(note: Note): Note =
        apiService.createNote(CreateNoteRequest(note.title, note.content))

    override suspend fun updateNote(note: Note): Note =
        throw UnsupportedOperationException("updateNote is not supported by the remote API")

    override suspend fun deleteNote(id: String): Boolean {
        apiService.deleteNote(id)
        return true
    }
}
