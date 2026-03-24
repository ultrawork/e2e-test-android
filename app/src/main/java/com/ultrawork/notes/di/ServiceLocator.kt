package com.ultrawork.notes.di

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.data.repository.NotesRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * Service locator that provides dependencies for the network layer.
 *
 * When [apiBaseUrl] is blank, [notesRepository] returns null and no network
 * infrastructure is created.
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
            .registerTypeAdapter(Date::class.java, object : TypeAdapter<Date>() {
                override fun write(out: JsonWriter, value: Date?) {
                    out.value(value?.let { DateTimeFormatter.ISO_INSTANT.format(it.toInstant()) })
                }
                override fun read(`in`: JsonReader): Date? {
                    if (`in`.peek() == com.google.gson.stream.JsonToken.NULL) {
                        `in`.nextNull()
                        return null
                    }
                    return Date.from(Instant.parse(`in`.nextString()))
                }
            })
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

    /** Retrofit-backed [NotesRepository] instance, or null if [apiBaseUrl] is blank. */
    val notesRepository: NotesRepository? by lazy {
        if (apiBaseUrl.isBlank()) null
        else NotesRepositoryImpl(apiService)
    }
}
