package com.ultrawork.notes.di

import com.ultrawork.notes.BuildConfig
import com.ultrawork.notes.data.remote.AuthInterceptor
import com.ultrawork.notes.data.remote.NotesApi
import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.data.repository.NotesRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val baseUrl = BuildConfig.API_BASE_URL.let {
            if (it.endsWith("/")) it else "$it/"
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNotesApi(retrofit: Retrofit): NotesApi {
        return retrofit.create(NotesApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNotesRepository(impl: NotesRepositoryImpl): NotesRepository {
        return impl
    }
}
