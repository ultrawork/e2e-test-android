package com.ultrawork.notes.data.remote

import com.ultrawork.notes.data.Category
import com.ultrawork.notes.model.Note
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API-интерфейс для работы с бэкендом заметок.
 */
interface ApiService {

    // --- Categories ---

    @GET("categories")
    suspend fun getCategories(): List<Category>

    @GET("categories/{id}")
    suspend fun getCategory(@Path("id") id: String): Category

    @POST("categories")
    suspend fun createCategory(@Body category: Category): Category

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: String, @Body category: Category): Category

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<Unit>

    // --- Notes ---

    @GET("notes")
    suspend fun getNotes(@Query("category") categoryId: String? = null): List<Note>

    @GET("notes/{id}")
    suspend fun getNote(@Path("id") id: Long): Note

    @POST("notes")
    suspend fun createNote(@Body note: Note): Note

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: Long, @Body note: Note): Note

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: Long): Response<Unit>
}
