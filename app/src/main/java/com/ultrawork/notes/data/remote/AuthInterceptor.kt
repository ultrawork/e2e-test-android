package com.ultrawork.notes.data.remote

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that attaches Authorization: Bearer <token> header
 * to all HTTP requests when an auth token is present in SharedPreferences.
 */
class AuthInterceptor(
    private val prefs: SharedPreferences
) : Interceptor {

    companion object {
        const val KEY_AUTH_TOKEN = "auth_token"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = prefs.getString(KEY_AUTH_TOKEN, null)

        val request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
