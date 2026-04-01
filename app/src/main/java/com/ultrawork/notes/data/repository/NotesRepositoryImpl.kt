package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiException
import com.ultrawork.notes.data.remote.NoteDto
import com.ultrawork.notes.data.remote.NoteRequest
import com.ultrawork.notes.data.remote.NotesApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class NotesRepositoryImpl @Inject constructor(
    private val api: NotesApi
) : NotesRepository {

    override suspend fun getNotes(): Result<List<NoteDto>> = safeApiCall {
        val response = api.getNotes()
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw ApiException(response.code(), response.message())
        }
    }

    override suspend fun createNote(request: NoteRequest): Result<NoteDto> = safeApiCall {
        val response = api.createNote(request)
        if (response.isSuccessful) {
            response.body() ?: throw ApiException(response.code(), "Empty response body")
        } else {
            throw ApiException(response.code(), response.message())
        }
    }

    override suspend fun deleteNote(id: String): Result<Unit> = safeApiCall {
        val response = api.deleteNote(id)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), response.message())
        }
    }

    private suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
