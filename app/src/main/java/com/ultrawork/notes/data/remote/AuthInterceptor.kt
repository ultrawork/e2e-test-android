package com.ultrawork.notes.data.remote

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp-интерсептор, добавляющий заголовок Authorization: Bearer <token>
 * к каждому запросу, если JWT-токен сохранён в SharedPreferences.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sharedPreferences.getString(KEY_TOKEN, null)
        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }

    companion object {
        const val KEY_TOKEN = "jwt_token"
    }
}
