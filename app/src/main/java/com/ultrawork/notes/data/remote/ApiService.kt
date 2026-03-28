package com.ultrawork.notes.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** DTO заметки, возвращаемой API. */
data class NoteDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

/** Тело запроса на создание заметки. */
data class CreateNoteRequest(
    val title: String,
    val content: String
)

/** Ответ на запрос dev-токена. */
data class DevTokenResponse(
    val token: String
)

/** Retrofit-интерфейс для работы с Notes API. */
interface ApiService {

    @GET("notes")
    suspend fun getNotes(): List<NoteDto>

    @POST("notes")
    suspend fun createNote(@Body request: CreateNoteRequest): NoteDto

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String)

    @POST("auth/dev-token")
    suspend fun getDevToken(): DevTokenResponse
}
