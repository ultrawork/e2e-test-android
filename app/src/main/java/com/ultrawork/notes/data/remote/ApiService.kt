package com.ultrawork.notes.data.remote

import com.ultrawork.notes.model.Note
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit service interface for the notes API.
 */
interface ApiService {

    @GET("notes")
    suspend fun getNotes(): List<Note>

    @POST("notes")
    suspend fun createNote(@Body request: CreateNoteRequest): Note

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: Long): Unit

    @PATCH("notes/{id}/toggle-favorite")
    suspend fun toggleFavorite(@Path("id") id: Long): Note
}

/**
 * Request body for creating a new note.
 */
data class CreateNoteRequest(
    val title: String,
    val content: String
)
