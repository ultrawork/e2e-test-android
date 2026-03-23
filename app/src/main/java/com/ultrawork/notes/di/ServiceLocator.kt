package com.ultrawork.notes.di

import com.google.gson.GsonBuilder
import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.repository.FakeNotesRepository
import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.data.repository.NotesRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Service locator that provides repository instances based on configuration.
 *
 * When [apiBaseUrl] is blank, returns [FakeNotesRepository].
 * When [apiBaseUrl] is non-blank, creates a Retrofit-backed [NotesRepositoryImpl].
 */
class DefaultServiceLocator(private val apiBaseUrl: String) {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    private val gson by lazy {
        GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(apiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    /**
     * Returns the appropriate [NotesRepository] implementation.
     */
    fun provideNotesRepository(): NotesRepository =
        if (apiBaseUrl.isBlank()) {
            FakeNotesRepository()
        } else {
            NotesRepositoryImpl(apiService)
        }
}
