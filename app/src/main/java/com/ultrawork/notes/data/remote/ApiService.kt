package com.ultrawork.notes.data.remote

import com.ultrawork.notes.data.remote.dto.CreateNoteRequest
import com.ultrawork.notes.data.remote.dto.NoteDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit API service for notes CRUD operations.
 */
interface ApiService {

    @GET("api/notes")
    suspend fun getNotes(): List<NoteDto>

    @POST("api/notes")
    suspend fun createNote(@Body request: CreateNoteRequest): NoteDto

    @DELETE("api/notes/{id}")
    suspend fun deleteNote(@Path("id") id: String)
}
