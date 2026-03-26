package com.ultrawork.notes.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit API service for notes operations.
 */
interface ApiService {

    @GET("notes")
    suspend fun getNotes(): List<NoteDto>

    @POST("notes")
    suspend fun createNote(@Body body: CreateNoteRequest): NoteDto

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: Long): Response<Unit>
}

/**
 * Data transfer object for note responses from the API.
 */
data class NoteDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: Long? = null,
    @SerializedName("updatedAt") val updatedAt: Long? = null
)

/**
 * Request body for creating a new note.
 */
data class CreateNoteRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String
)
