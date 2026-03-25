package com.ultrawork.notes.data.repository

import android.content.SharedPreferences
import com.ultrawork.notes.BuildConfig
import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.remote.AuthInterceptor.Companion.KEY_JWT_TOKEN
import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.data.remote.NoteDto
import com.ultrawork.notes.model.Note
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val prefs: SharedPreferences
) : NotesRepository {

    override suspend fun getNotes(): List<Note> {
        return apiService.getNotes().map { it.toNote() }
    }

    override suspend fun createNote(title: String, content: String): Note {
        return apiService.createNote(CreateNoteRequest(title, content)).toNote()
    }

    override suspend fun deleteNote(id: String) {
        apiService.deleteNote(id)
    }

    override suspend fun ensureDevToken() {
        if (BuildConfig.DEBUG && prefs.getString(KEY_JWT_TOKEN, null) == null) {
            val response = apiService.getDevToken()
            prefs.edit().putString(KEY_JWT_TOKEN, response.token).apply()
        }
    }

    private fun NoteDto.toNote(): Note = Note(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
