package com.ultrawork.notes.data.remote

import com.ultrawork.notes.model.Note
import retrofit2.http.GET
import retrofit2.http.Path

interface NotesApiService {
    @GET("notes/{id}")
    suspend fun getNoteById(@Path("id") id: String): Note
}
