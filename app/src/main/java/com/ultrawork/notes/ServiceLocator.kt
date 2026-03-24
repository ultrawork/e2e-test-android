package com.ultrawork.notes

import com.ultrawork.notes.network.ApiService
import com.ultrawork.notes.repository.NotesRepository
import com.ultrawork.notes.repository.NotesRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Service Locator — синглтон для создания и хранения сетевых зависимостей.
 */
object ServiceLocator {

    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val notesRepository: NotesRepository by lazy {
        NotesRepositoryImpl(apiService)
    }
}
