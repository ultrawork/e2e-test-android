package com.ultrawork.notes.data.remote

import com.ultrawork.notes.model.Note
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service for notes backend.
 */
interface ApiService {

    @GET("notes")
    suspend fun getNotes(
        @Query("favoritesOnly") favoritesOnly: Boolean? = null
    ): List<Note>

    @PATCH("notes/{id}/favorite")
    suspend fun toggleFavorite(
        @Path("id") id: String
    ): Note
}
