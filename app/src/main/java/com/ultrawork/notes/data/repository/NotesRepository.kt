package com.ultrawork.notes.data.repository

import com.ultrawork.notes.model.Note

interface NotesRepository {

    suspend fun getNotes(): List<Note>

    suspend fun createNote(title: String, content: String): Note

    suspend fun deleteNote(id: String)

    suspend fun ensureDevToken()
}
