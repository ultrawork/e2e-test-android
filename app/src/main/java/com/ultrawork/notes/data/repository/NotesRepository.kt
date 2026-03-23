package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.model.Note
import javax.inject.Inject

/**
 * Repository for notes operations. Delegates to the REST API.
 * Returns an empty list on failure to gracefully handle missing/invalid tokens.
 */
class NotesRepository @Inject constructor(private val api: ApiService) {

    suspend fun getNotes(): List<Note> =
        runCatching { api.fetchNotes() }.getOrDefault(emptyList())

    suspend fun addNote(title: String, content: String): Note =
        api.createNote(mapOf("title" to title, "content" to content))

    suspend fun removeNote(id: String) {
        api.deleteNote(id)
    }
}
