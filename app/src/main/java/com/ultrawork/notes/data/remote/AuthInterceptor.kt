package com.ultrawork.notes.data.remote

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds Authorization Bearer token from SharedPreferences to every request.
 */
class AuthInterceptor(
    private val sharedPreferences: SharedPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = sharedPreferences.getString("auth_token", null)

        val newRequest = if (!token.isNullOrBlank()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(newRequest)
    }
}
