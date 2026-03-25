package com.ultrawork.notes.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("notes")
    suspend fun getNotes(): List<NoteDto>

    @POST("notes")
    suspend fun createNote(@Body request: CreateNoteRequest): NoteDto

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<Unit>

    @POST("auth/dev-token")
    suspend fun getDevToken(): DevTokenResponse
}
