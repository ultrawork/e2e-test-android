package com.ultrawork.notes.data.api

import com.ultrawork.notes.data.dto.CreateNoteRequest
import com.ultrawork.notes.data.dto.UpdateNoteRequest
import com.ultrawork.notes.model.Category
import com.ultrawork.notes.model.Note
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit API service for notes and categories endpoints.
 */
interface ApiService {

    @GET("notes")
    suspend fun getNotes(): List<Note>

    @GET("notes/{id}")
    suspend fun getNote(@Path("id") id: String): Note

    @POST("notes")
    suspend fun createNote(@Body body: CreateNoteRequest): Note

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: String, @Body body: UpdateNoteRequest): Note

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<Unit>

    @GET("categories")
    suspend fun getCategories(): List<Category>
}
