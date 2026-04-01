package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiException
import com.ultrawork.notes.data.remote.NoteDto
import com.ultrawork.notes.data.remote.NoteRequest
import com.ultrawork.notes.data.remote.NotesApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepositoryImpl @Inject constructor(
    private val api: NotesApi
) : NotesRepository {

    override suspend fun getNotes(): Result<List<NoteDto>> = runCatching {
        val response = api.getNotes()
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw ApiException(response.code(), response.message())
        }
    }

    override suspend fun createNote(request: NoteRequest): Result<NoteDto> = runCatching {
        val response = api.createNote(request)
        if (response.isSuccessful) {
            response.body() ?: throw ApiException(response.code(), "Empty response body")
        } else {
            throw ApiException(response.code(), response.message())
        }
    }

    override suspend fun deleteNote(id: String): Result<Unit> = runCatching {
        val response = api.deleteNote(id)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), response.message())
        }
    }
}
