package com.ultrawork.notes.data.remote

import com.ultrawork.notes.model.Note
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * REST API service for notes operations.
 */
interface ApiService {

    @GET("notes")
    suspend fun fetchNotes(): List<Note>

    @POST("notes")
    suspend fun createNote(@Body body: Map<String, String>): Note

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<Unit>
}
