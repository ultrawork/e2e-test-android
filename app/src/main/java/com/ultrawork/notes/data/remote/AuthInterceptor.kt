package com.ultrawork.notes.data.remote

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that attaches Bearer token from SharedPreferences to every request.
 */
class AuthInterceptor(private val prefs: SharedPreferences) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = prefs.getString("token", null)
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
