package com.ultrawork.notes.data.remote

import com.ultrawork.notes.model.Note
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Ответ на запрос dev-токена. */
data class DevTokenResponse(val token: String)

/** Тело запроса для создания заметки. */
data class CreateNoteRequest(val title: String, val content: String)

/** Тело запроса для обновления заметки. */
data class UpdateNoteRequest(val title: String, val content: String)

/**
 * Retrofit-интерфейс для взаимодействия с backend API.
 */
interface ApiService {

    @POST("auth/dev-token")
    suspend fun getDevToken(): DevTokenResponse

    @GET("notes")
    suspend fun getNotes(): List<Note>

    @GET("notes/{id}")
    suspend fun getNote(@Path("id") id: String): Note

    @POST("notes")
    suspend fun createNote(@Body request: CreateNoteRequest): Note

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: String, @Body request: UpdateNoteRequest): Note

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<Unit>
}
