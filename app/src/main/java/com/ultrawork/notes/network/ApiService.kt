package com.ultrawork.notes.network

import com.ultrawork.notes.model.Note
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit-интерфейс для работы с backend API заметок.
 */
interface ApiService {

    @GET("notes")
    suspend fun getNotes(): List<Note>

    @POST("notes")
    suspend fun createNote(@Body request: CreateNoteRequest): Note

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String)

    @PATCH("notes/{id}/favorite")
    suspend fun toggleFavorite(@Path("id") id: String): Note
}

/**
 * DTO для создания заметки.
 */
data class CreateNoteRequest(
    val title: String,
    val content: String
)
