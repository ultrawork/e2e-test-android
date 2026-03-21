package com.ultrawork.notes.di

import com.google.gson.GsonBuilder
import com.ultrawork.notes.BuildConfig
import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.repository.CategoryRepository
import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.data.repository.fake.FakeCategoryRepository
import com.ultrawork.notes.data.repository.fake.FakeNotesRepository
import com.ultrawork.notes.data.repository.impl.CategoryRepositoryImpl
import com.ultrawork.notes.data.repository.impl.NotesRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Простой сервис-локатор: по умолчанию отдаёт Fake-репозитории,
 * при наличии непустого [BuildConfig.API_BASE_URL] — собирает Retrofit и реальные репозитории.
 */
object DefaultServiceLocator {

    private val fakeCategoryRepository: FakeCategoryRepository by lazy { FakeCategoryRepository() }
    private val fakeNotesRepository: FakeNotesRepository by lazy { FakeNotesRepository() }

    private val apiService: ApiService? by lazy {
        val baseUrl = BuildConfig.API_BASE_URL
        if (baseUrl.isBlank()) return@lazy null

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    /** Возвращает [CategoryRepository]: Fake или реальный в зависимости от API_BASE_URL. */
    fun provideCategoryRepository(): CategoryRepository {
        val api = apiService ?: return fakeCategoryRepository
        return CategoryRepositoryImpl(api)
    }

    /** Возвращает [NotesRepository]: Fake или реальный в зависимости от API_BASE_URL. */
    fun provideNotesRepository(): NotesRepository {
        val api = apiService ?: return fakeNotesRepository
        return NotesRepositoryImpl(api)
    }
}
