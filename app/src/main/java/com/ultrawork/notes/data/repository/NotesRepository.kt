package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.NoteDto
import com.ultrawork.notes.data.remote.NoteRequest

interface NotesRepository {
    suspend fun getNotes(): Result<List<NoteDto>>
    suspend fun createNote(request: NoteRequest): Result<NoteDto>
    suspend fun deleteNote(id: String): Result<Unit>
}
