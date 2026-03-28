package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.model.Note
import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.data.remote.NoteDto
import javax.inject.Inject
import javax.inject.Singleton

/** Реализация [NotesRepository], выполняющая запросы через [ApiService]. */
@Singleton
class NotesRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : NotesRepository {

    override suspend fun getNotes(): Result<List<Note>> = runCatching {
        apiService.getNotes().map { it.toDomain() }
    }

    override suspend fun createNote(title: String, content: String): Result<Note> = runCatching {
        apiService.createNote(CreateNoteRequest(title, content)).toDomain()
    }

    override suspend fun deleteNote(id: String): Result<Unit> = runCatching {
        apiService.deleteNote(id)
    }

    private fun NoteDto.toDomain(): Note = Note(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
