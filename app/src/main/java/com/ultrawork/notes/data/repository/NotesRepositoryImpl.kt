package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.data.remote.NoteDto
import com.ultrawork.notes.model.Note
import retrofit2.HttpException
import java.util.Date

/**
 * Repository implementation that delegates to the remote API service.
 */
class NotesRepositoryImpl(
    private val api: ApiService
) : NotesRepository {

    override suspend fun getNotes(): List<Note> {
        return api.getNotes().map { it.toDomain() }
    }

    override suspend fun createNote(title: String, content: String): Note {
        val request = CreateNoteRequest(title = title, content = content)
        return api.createNote(request).toDomain()
    }

    override suspend fun deleteNote(id: Long) {
        val response = api.deleteNote(id)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
}

private fun NoteDto.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt?.let { Date(it) } ?: Date(),
    updatedAt = updatedAt?.let { Date(it) } ?: Date()
)
